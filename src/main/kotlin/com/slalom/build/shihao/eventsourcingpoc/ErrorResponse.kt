package com.slalom.build.shihao.eventsourcingpoc

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class ResourceNotFoundException : ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")

class BadRequestException(message: String) : ResponseStatusException(HttpStatus.BAD_REQUEST, message)