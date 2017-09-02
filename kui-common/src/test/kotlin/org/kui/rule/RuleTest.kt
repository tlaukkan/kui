package org.kui.security

import org.apache.log4j.xml.DOMConfigurator
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.kui.rule.parseIfThenRule

class RuleTest {

    @Test
    @Ignore
    fun testParsing() {
        DOMConfigurator.configure("log4j.xml")
        Assert.assertEquals(
                "IfThenRule(ifFunction=activity, ifFunctionParameters={environmentType=home, log=C:\\Logs\\white-ice-server.log, minutes=15, minOccurenes=1}, thenFunction=notify, thenFunctionParameters={group=user})",
                parseIfThenRule("if (activity(environmentType='home',log='C:\\Logs\\white-ice-server.log',minutes=15,minOccurenes=1)) then notify(group='user')").toString()
        )

        Assert.assertEquals(
                "IfThenRule(ifFunction=activity, ifFunctionParameters={environmentType=home, log=C:\\Logs\\white-ice-server.log, minutes=15, minOccurenes=1}, thenFunction=notify, thenFunctionParameters={group=user})",
                parseIfThenRule("if(activity(environmentType='home',log='C:\\Logs\\white-ice-server.log',minutes=15,minOccurenes=1))then notify(group='user')").toString()
        )

        Assert.assertEquals(
                "IfThenRule(ifFunction=activity, ifFunctionParameters={environmentType=hom\\'e, log=C:\\Logs\\white-ice-server.log, minutes=15, minOccurenes=1}, thenFunction=notify, thenFunctionParameters={group=user})",
                parseIfThenRule("if (activity(environmentType='hom\\'e',log='C:\\Logs\\white-ice-server.log',minutes=15,minOccurenes=1)) then notify(group='user')").toString()
        )

        Assert.assertEquals(
                "IfThenRule(ifFunction=activity, ifFunctionParameters={environmentType= then , log=C:\\Logs\\white-ice-server.log, minutes=15, minOccurenes=1}, thenFunction=notify, thenFunctionParameters={group=user})",
                parseIfThenRule("if (activity(environmentType=' then ',log='C:\\Logs\\white-ice-server.log',minutes=15, minOccurenes=1)) then notify(group='user')").toString()
        )
    }

}