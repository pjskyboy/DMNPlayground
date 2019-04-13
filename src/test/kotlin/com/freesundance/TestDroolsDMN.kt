package com.freesundance

import mu.KotlinLogging
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test
import org.kie.dmn.api.core.event.AfterEvaluateDecisionTableEvent
import org.kie.dmn.api.core.event.DMNRuntimeEventListener
import org.kie.dmn.core.api.DMNFactory
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.hamcrest.CoreMatchers.`is` as Is

class TestDroolsDMN {

    private val logger = KotlinLogging.logger {}

    @Test
    fun testMultiOutputDecision() {

        val runtime = DMNRuntimeUtil.createRuntime("simple_multi_output_dmn-1.1.dmn", this.javaClass)
        val listener = Mockito.mock(DMNRuntimeEventListener::class.java)
        runtime.addListener(listener)

        val dmnModel = runtime.getModel("http://www.trisotech.com/definitions/_b27ca105-c515-498f-81e9-b28cd10a628e",
                "simple_multi_output_dmn")

        val context = DMNFactory.newContext()
        context.set("Input1", "123")
        val dmnResult = runtime.evaluateAll(dmnModel, context)

        assertThat<Boolean>(dmnResult.hasErrors(), Is<Boolean>(false))

        val result = dmnResult.context
        logger.info("$result")

        val decisionLogic1Results = result.get("Decision Logic 1") as Map<*, *>

        assertThat<String>(decisionLogic1Results["part1"] as String, Is(equalTo("abc")))
        assertThat<String>(decisionLogic1Results["part2"] as String, Is(equalTo("111")))

        val captor = ArgumentCaptor.forClass (AfterEvaluateDecisionTableEvent::class.java)
        verify<DMNRuntimeEventListener>(listener, times(1)).afterEvaluateDecisionTable(captor.capture())

        val first = captor.allValues[0]
        assertThat(first.matches, Is<List<Int>>(listOf(1)))
        assertThat(first.selected, Is<List<Int>>(listOf(1)))
    }
}