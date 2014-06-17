package com.ericsson.deviceaccess.adaptor.ruleengine.device;

/*
 * Copyright (c) 2003-2011, KNOPFLERFISH project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the KNOPFLERFISH project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;

public class LDAPExpr {

    public static final int AND = 0;
    public static final int OR = 1;
    public static final int NOT = 2;
    public static final int EQ = 4;
    public static final int LE = 8;
    public static final int GE = 16;
    public static final int APPROX = 32;
    public static final int COMPLEX = AND | OR | NOT;
    public static final int SIMPLE = EQ | LE | GE | APPROX;

    private static final char WILDCARD = 65535;
    private static final String WILDCARD_STRING = new String(new char[]{WILDCARD});

    private static final String NULL = "Null query";
    private static final String GARBAGE = "Trailing garbage";
    private static final String EOS = "Unexpected end of query";
    private static final String MALFORMED = "Malformed query";
    private static final String OPERATOR = "Undefined operator";
    // Clazz -> Constructor(String)
    private static Map<Class<?>, Constructor<?>> constructorMap = new ConcurrentHashMap<>();
    private static Constructor<?> DUMMY_CONS = LDAPExpr.class.getConstructors()[0];

    public static boolean query(String filter, Map<String, Object> pd) throws InvalidSyntaxException {
        return new LDAPExpr(filter).evaluate(pd, false);
    }

    /**
     * Get cached String constructor for a class
     */
    private static Constructor getConstructor(Class clazz) {
        Constructor<?> cons = constructorMap.computeIfAbsent(clazz, k -> {
            try {
                return clazz.getConstructor(String.class);
            } catch (NoSuchMethodException | SecurityException ex) {
                return DUMMY_CONS;
            }
        });
        if (cons == DUMMY_CONS) {
            return null;
        }
        return cons;
    }

    private static boolean compareString(String s1, int op, String s2) {
        switch (op) {
            case LE:
                return s1.compareTo(s2) <= 0;
            case GE:
                return s1.compareTo(s2) >= 0;
            case EQ:
                return patSubstr(s1, s2);
            case APPROX:
                return fixupString(s2).equals(fixupString(s1));
            default:
                return false;
        }
    }

    private static String fixupString(String s) {
        return s.replaceAll("\\s+", "").toLowerCase();
    }

    private static boolean patSubstr(String s, String pat) {
        return s == null ? false : patSubstr(s.toCharArray(), 0, pat.toCharArray(), 0);
    }

    private static boolean patSubstr(char[] s, int si, char[] pat, int pi) {
        if (pat.length - pi == 0) {
            return s.length - si == 0;
        }
        if (pat[pi] == WILDCARD) {
            pi++;
            for (;;) {
                if (patSubstr(s, si, pat, pi)) {
                    return true;
                }
                if (s.length - si == 0) {
                    return false;
                }
                si++;
            }
        } else {
            if (s.length - si == 0) {
                return false;
            }
            if (s[si] != pat[pi]) {
                return false;
            }
            return patSubstr(s, ++si, pat, ++pi);
        }
    }

    private static LDAPExpr parseExpr(ParseState ps) throws InvalidSyntaxException {
        ps.skipWhite();
        if (!ps.prefix("(")) {
            ps.error(MALFORMED);
        }

        int operator;
        ps.skipWhite();
        switch (ps.peek()) {
            case '&':
                operator = AND;
                break;
            case '|':
                operator = OR;
                break;
            case '!':
                operator = NOT;
                break;
            default:
                return parseSimple(ps);
        }
        ps.skip(1); // Ignore the operator
        List v = new ArrayList();
        do {
            v.add(parseExpr(ps));
            ps.skipWhite();
        } while (ps.peek() == '(');
        int n = v.size();
        if (!ps.prefix(")") || n == 0 || (operator == NOT && n > 1)) {
            ps.error(MALFORMED);
        }
        LDAPExpr[] args = new LDAPExpr[n];
        v.toArray(args);
        return new LDAPExpr(operator, args);
    }

    private static LDAPExpr parseSimple(ParseState ps) throws InvalidSyntaxException {
        String attrName = ps.getAttributeName();
        if (attrName == null) {
            ps.error(MALFORMED);
        }
        int operator = 0;
        if (ps.prefix("=")) {
            operator = EQ;
        } else if (ps.prefix("<=")) {
            operator = LE;
        } else if (ps.prefix(">=")) {
            operator = GE;
        } else if (ps.prefix("~=")) {
            operator = APPROX;
        } else {
            // System.out.println("undef op='" + ps.peek() + "'");
            ps.error(OPERATOR); // Does not return
        }
        String attrValue = ps.getAttributeValue();
        if (!ps.prefix(")")) {
            ps.error(MALFORMED);
        }
        return new LDAPExpr(operator, attrName, attrValue);
    }

    public static void main(String[] args) {
        try {
            LDAPExpr l = new LDAPExpr("(prop1&=bpan*)");
            Map<String, Object> p = new HashMap<>();
            p.put("prop1", "apanrap");
            System.out.println(l.evaluate(p, true));
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }
    }

    public int operator;
    public LDAPExpr[] args;
    public String attrName;
    public String attrValue;

    public LDAPExpr(String filter) throws InvalidSyntaxException {
        ParseState ps = new ParseState(filter);
        try {
            LDAPExpr expr = parseExpr(ps);
            if (ps.rest().trim().length() != 0) {
                ps.error(GARBAGE + " '" + ps.rest() + "'");
            }
            operator = expr.operator;
            args = expr.args;
            attrName = expr.attrName;
            attrValue = expr.attrValue;
        } catch (StringIndexOutOfBoundsException e) {
            ps.error(EOS);
        }

    }

    private LDAPExpr(int operator, LDAPExpr[] args) {
        this.operator = operator;
        this.args = args;
        this.attrName = null;
        this.attrValue = null;
    }

    private LDAPExpr(int operator, String attrName, String attrValue) {
        this.operator = operator;
        this.args = null;
        this.attrName = attrName;
        this.attrValue = attrValue;
    }

    /**
     * Get object class set matched by this LDAP expression. This will not work
     * with wildcards and NOT expressions. If a set can not be determined return
     * null.
     *
     * @return Set of classes matched, otherwise <code>null</code>.
     */
    public Set getMatchedObjectClasses() {
        Set objClasses = null;
        if (operator == EQ) {
            if (attrName.equalsIgnoreCase(Constants.OBJECTCLASS) && attrValue.indexOf(WILDCARD) < 0) {
                objClasses = new OneSet(attrValue);
            }
        } else if (operator == AND) {
            for (LDAPExpr arg : args) {
                Set r = arg.getMatchedObjectClasses();
                if (r != null) {
                    if (objClasses == null) {
                        objClasses = r;
                    } else {
                        // if AND op and classes in several operands,
                        // then only the intersection is possible.
                        if (objClasses instanceof OneSet) {
                            objClasses = new TreeSet(objClasses);
                        }
                        objClasses.retainAll(r);
                    }
                }
            }
        } else if (operator == OR) {
            for (LDAPExpr arg : args) {
                Set r = arg.getMatchedObjectClasses();
                if (r != null) {
                    if (objClasses == null) {
                        objClasses = new TreeSet();
                    }
                    objClasses.addAll(r);
                } else {
                    objClasses = null;
                    break;
                }
            }
        }
        return objClasses;
    }

    /**
     * Checks if this LDAP expression is "simple". The definition of a simple
     * filter is:
     * <ul>
     * <li><code>(<it>name</it>=<it>value</it>)</code> is simple if
     * <it>name</it> is a member of the provided <code>keywords</code>, and
     * <it>value</it> does not contain a wildcard character;</li>
     * <li><code>(| EXPR+ )</code> is simple if all <code>EXPR</code>
     * expressions are simple;</li>
     * <li>No other expressions are simple.</li>
     * </ul>
     * If the filter is found to be simple, the <code>cache</code> is filled
     * with mappings from the provided keywords to lists of attribute values.
     * The keyword-value-pairs are the ones that satisfy this expression, for
     * the given keywords.
     *
     * @param keywords The keywords to look for.
     * @param cache An array (indexed by the keyword indexes) of lists to fill
     * in with values saturating this expression.
     * @param matchCase
     * @return <code>true</code> if this expression is simple,
     * <code>false</code> otherwise.
     */
    public boolean isSimple(List keywords, List[] cache, boolean matchCase) {
        if (operator == EQ) {
            int index;
            if ((index = keywords.indexOf(matchCase ? attrName : attrName.toLowerCase())) >= 0 && attrValue.indexOf(WILDCARD) < 0) {
                if (cache[index] == null) {
                    cache[index] = new ArrayList();
                }
                cache[index].add(attrValue);
                return true;
            }
        } else if (operator == OR) {
            for (LDAPExpr arg : args) {
                if (!arg.isSimple(keywords, cache, matchCase)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Evaluate this LDAP filter.
     *
     * @param p
     * @param matchCase
     * @return
     */
    public boolean evaluate(Map<String, Object> p, boolean matchCase) {
        if ((operator & SIMPLE) != 0) {
            return compare(p.get(matchCase ? attrName : attrName.toLowerCase()), operator, attrValue);
        } else { // (operator & COMPLEX) != 0
            switch (operator) {
                case AND:
                    for (LDAPExpr arg : args) {
                        if (!arg.evaluate(p, matchCase)) {
                            return false;
                        }
                    }
                    return true;
                case OR:
                    for (LDAPExpr arg : args) {
                        if (arg.evaluate(p, matchCase)) {
                            return true;
                        }
                    }
                    return false;
                case NOT:
                    return !args[0].evaluate(p, matchCase);
                default:
                    return false; // Cannot happen
            }
        }
    }

    public List<String> getAttributeNames() {
        List<String> res = new LinkedList<>();
        if ((operator & SIMPLE) != 0) {
            res.add(attrName);
            return res;
        } else {// (operator & COMPLEX) != 0
            for (LDAPExpr arg : args) {
                res.addAll(arg.getAttributeNames());
            }
            return res;
        }
    }

    public List<LDAPExpr> findAttributes(String name) {
        List<LDAPExpr> res = new LinkedList<>();
        if ((operator & SIMPLE) != 0) {
            if (name.equals(attrName)) {
                res.add(this);
            }
            return res;
        } else {  // (operator & COMPLEX) != 0
            for (LDAPExpr arg : args) {
                res.addAll(arg.findAttributes(name));
            }
            return res;
        }
    }

    public List<LDAPExpr> findValues(String val) {
        List<LDAPExpr> res = new LinkedList<>();
        if ((operator & SIMPLE) != 0) {
            if (val.equals(attrValue)) {
                res.add(this);
            }
            return res;
        } else { // (operator & COMPLEX) != 0
            for (LDAPExpr arg : args) {
                res.addAll(arg.findValues(val));
            }
            return res;
        }
    }

    /**
     * ** Private methods **
     *
     *
     * @param obj
     * @param op
     * @param s
     * @return
     */
    protected boolean compare(Object obj, int op, String s) {
        if (obj == null) {
            return false;
        }
        if (op == EQ && s.equals(WILDCARD_STRING)) {
            return true;
        }
        try {
            if (obj instanceof String || obj instanceof Character) {
                return compareString(obj.toString(), op, s);
            } else if (obj instanceof Boolean) {
                if (op == LE || op == GE) {
                    return false;
                }
                if ((Boolean) obj) {
                    return s.equalsIgnoreCase("true");
                } else {
                    return s.equalsIgnoreCase("false");
                }
            } else if (obj instanceof Number) {
                if (obj instanceof Byte) {
                    switch (op) {
                        case LE:
                            return ((int) obj) <= Byte.parseByte(s);
                        case GE:
                            return ((int) obj) >= Byte.parseByte(s);
                        default: /* APPROX and EQ */

                            return new Byte(s).equals(obj);
                    }
                } else if (obj instanceof Integer) {
                    switch (op) {
                        case LE:
                            return ((Integer) obj) <= Integer.parseInt(s);
                        case GE:
                            return ((Integer) obj) >= Integer.parseInt(s);
                        default: /* APPROX and EQ */

                            return new Integer(s).equals(obj);
                    }
                } else if (obj instanceof Short) {
                    switch (op) {
                        case LE:
                            return ((int) obj) <= Short.parseShort(s);
                        case GE:
                            return ((int) obj) >= Short.parseShort(s);
                        default: /* APPROX and EQ */

                            return new Short(s).equals(obj);
                    }
                } else if (obj instanceof Long) {
                    switch (op) {
                        case LE:
                            return ((Long) obj) <= Long.parseLong(s);
                        case GE:
                            return ((Long) obj) >= Long.parseLong(s);
                        default: /* APPROX and EQ */

                            return new Long(s).equals(obj);
                    }
                } else if (obj instanceof Float) {
                    switch (op) {
                        case LE:
                            return ((Float) obj) <= (new Float(s));
                        case GE:
                            return ((Float) obj) >= (new Float(s));
                        default: /* APPROX and EQ */

                            return new Float(s).equals(obj);
                    }
                } else if (obj instanceof Double) {
                    switch (op) {
                        case LE:
                            return ((Double) obj) <= (new Double(s));
                        case GE:
                            return ((Double) obj) >= (new Double(s));
                        default: /* APPROX and EQ */

                            return new Double(s).equals(obj);
                    }
                } else if (BigInteger.class.isInstance(obj)) {
                    int c = ((Comparable<BigInteger>) obj).compareTo(new BigInteger(s));
                    switch (op) {
                        case LE:
                            return c <= 0;
                        case GE:
                            return c >= 0;
                        default: /* APPROX and EQ */

                            return c == 0;
                    }
                } else if (BigDecimal.class.isInstance(obj)) {
                    int c = ((Comparable<BigDecimal>) obj).compareTo(new BigDecimal(s));
                    switch (op) {
                        case LE:
                            return c <= 0;
                        case GE:
                            return c >= 0;
                        default: /* APPROX and EQ */

                            return c == 0;
                    }
                }
            } else if (obj instanceof Collection) {
                if (((Collection) obj).stream().anyMatch(i -> compare(i, op, s))) {
                    return true;
                }
            } else if (obj.getClass().isArray()) {
                int len = Array.getLength(obj);
                for (int i = 0; i < len; i++) {
                    if (compare(Array.get(obj, i), op, s)) {
                        return true;
                    }
                }
            } else {
                // Extended comparison
                // Allow simple EQ comparison on all classes having
                // a string constructor, and use compareTo if they
                // implement Comparable
                Constructor cons = getConstructor(obj.getClass());

                if (cons != null) {
                    Object other = cons.newInstance(s);
                    if (obj instanceof Comparable) {
                        int c = ((Comparable) obj).compareTo(other);
                        switch (op) {
                            case LE:
                                return c <= 0;
                            case GE:
                                return c >= 0;
                            default: /* APPROX and EQ */

                                return c == 0;
                        }
                    } else {
                        boolean b = false;
                        if (op == LE || op == GE || op == EQ || op == APPROX) {
                            b = obj.equals(other);
                        }
                        return b;
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException |
                IllegalAccessException |
                IllegalArgumentException |
                InstantiationException |
                InvocationTargetException ex) {
            // This might happen if a string-to-datatype conversion fails
            // Just consider it a false match and ignore the exception
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder();
        res.append("(");
        if ((operator & SIMPLE) != 0) {
            res.append(attrName);
            switch (operator) {
                case EQ:
                    res.append("=");
                    break;
                case LE:
                    res.append("<=");
                    break;
                case GE:
                    res.append(">=");
                    break;
                case APPROX:
                    res.append("~=");
                    break;
            }
            for (int i = 0; i < attrValue.length(); i++) {
                char c = attrValue.charAt(i);
                if (c == '(' || c == ')' || c == '*' || c == '\\') {
                    res.append('\\');
                } else if (c == WILDCARD) {
                    c = '*';
                }
                res.append(c);
            }
        } else {
            switch (operator) {
                case AND:
                    res.append("&");
                    break;
                case OR:
                    res.append("|");
                    break;
                case NOT:
                    res.append("!");
                    break;
            }
            for (LDAPExpr arg : args) {
                res.append(arg.toString());
            }
        }
        res.append(")");
        return res.toString();
    }

    /**
     * Contains the current parser position and parsing utility methods.
     */
    private static class ParseState {

        int pos;
        String str;

        ParseState(String str) throws InvalidSyntaxException {
            this.str = str;
            if (str.length() == 0) {
                error(NULL);
            }
            pos = 0;
        }

        public boolean prefix(String pre) {
            if (!str.startsWith(pre, pos)) {
                return false;
            }
            pos += pre.length();
            return true;
        }

        public char peek() {
            return str.charAt(pos);
        }

        public void skip(int n) {
            pos += n;
        }

        public String rest() {
            return str.substring(pos);
        }

        public void skipWhite() {
            while (Character.isWhitespace(str.charAt(pos))) {
                pos++;
            }
        }

        public String getAttributeName() {
            int start = pos;
            int end = -1;
            for (;; pos++) {
                char c = str.charAt(pos);
                if (c == '(' || c == ')' || c == '<' || c == '>' || c == '=' || c == '~') {
                    break;
                } else if (!Character.isWhitespace(c)) {
                    end = pos;
                }
            }
            if (end == -1) {
                return null;
            }
            return str.substring(start, end + 1);
        }

        public String getAttributeValue() {
            StringBuilder sb = new StringBuilder();
            label:
            for (;; pos++) {
                char c = str.charAt(pos);
                switch (c) {
                    case '(':
                    case ')':
                        break label;
                    case '*':
                        sb.append(WILDCARD);
                        break;
                    case '\\':
                        sb.append(str.charAt(++pos));
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            return sb.toString();
        }

        public void error(String m) throws InvalidSyntaxException {
            throw new InvalidSyntaxException(m, (str == null) ? "" : str.substring(pos));
        }
    }

    /**
     * Set with one element maximum.
     */
    private static class OneSet extends AbstractSet {

        final private Object elem;

        OneSet(Object o) {
            elem = o;
        }

        @Override
        public Iterator iterator() {
            return new Iterator() {
                Object ielem = elem;

                @Override
                public boolean hasNext() {
                    return ielem != null;
                }

                @Override
                public Object next() {
                    if (ielem != null) {
                        Object r = ielem;
                        ielem = null;
                        return r;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {
            return elem == null ? 0 : 1;
        }
    }

}
