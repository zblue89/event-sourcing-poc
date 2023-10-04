package com.slalom.build.shihao.eventsourcingpoc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EventSourcingPocApplication

fun main(args: Array<String>) {
	runApplication<EventSourcingPocApplication>(*args)
}
