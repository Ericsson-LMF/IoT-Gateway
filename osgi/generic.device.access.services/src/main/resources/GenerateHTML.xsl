<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="html" version="4.0" encoding="iso-8859-1" indent="yes"/>

    <xsl:import-schema schema-location="http://www.cf.ericsson.net/schema/services.xsd"/>

    <xsl:template match="/">
        <html>
            <body>
                <h2>Device Access Service Schemas</h2>
                <xsl:for-each select="service-schema/services/service">
                    <h3>
                        <xsl:value-of select="@name"/>
                    </h3>
                    <nobr><b>Category:</b>&#160;<xsl:value-of select="category"/>
                        <br/>
                    </nobr>
                    <nobr><b>Description:</b>&#160;<xsl:value-of select="description"/>
                        <br/>
                    </nobr>
                    <xsl:if test="count(actions/action) > 0">
                        <table border="0" cellspacing="1" cellpadding="1" bgcolor="#000000">
                            <tr bgcolor="#FFFFFF">
                                <th>Action</th>
                                <th>Arguments</th>
                                <th>Results</th>
                                <th>Description</th>
                            </tr>
                            <xsl:for-each select="actions/action">
                                <tr bgcolor="#FFFFFF">
                                    <td valign='top'>
                                        <xsl:value-of select="@name"/>
                                    </td>
                                    <td valign='top'>
                                        <table border="0" cellspacing="0" cellpadding="0">
                                            <xsl:for-each select="arguments/parameter">
                                                <tr>
                                                    <td><xsl:value-of select="@type"/>&#160;:&#160;<xsl:value-of
                                                            select="@name"/>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                    <td valign='top'>
                                        <table border="0" cellspacing="0" cellpadding="0">
                                            <xsl:for-each select="results/parameter">
                                                <tr>
                                                    <td><xsl:value-of select="@type"/>&#160;:&#160;<xsl:value-of
                                                            select="@name"/>
                                                    </td>
                                                </tr>
                                            </xsl:for-each>
                                        </table>
                                    </td>
                                    <td valign='top'>
                                        <xsl:value-of select="description"/>
                                    </td>
                                </tr>
                            </xsl:for-each>
                        </table>
                    </xsl:if>
                    <br/>
                    <xsl:if test="count(properties/parameter) > 0">
                        <table border="0" cellspacing="1" cellpadding="1" bgcolor="#000000">
                            <tr bgcolor="#FFFFFF">
                                <th>Property</th>
                                <th>Type</th>
                                <th>Description</th>
                            </tr>
                            <xsl:for-each select="properties/parameter">
                                <tr bgcolor="#FFFFFF">
                                    <td valign='top'>
                                        <xsl:value-of select="@name"/>
                                    </td>
                                    <td valign='top'>
                                        <xsl:value-of select="@type"/>
                                    </td>
                                    <td valign='top'>
                                        <xsl:value-of select="description"/>
                                    </td>
                                </tr>
                            </xsl:for-each>
                        </table>
                    </xsl:if>
                </xsl:for-each>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>
