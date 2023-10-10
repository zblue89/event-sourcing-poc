package com.slalom.build.shihao.eventsourcingpoc

import com.amazonaws.serverless.exceptions.ContainerInitializationException
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.AwsProxyResponse
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class StreamLambdaHandler : RequestStreamHandler {
    @Throws(IOException::class)
    override fun handleRequest(inputStream: InputStream, outputStream: OutputStream, context: Context) {
        handler.proxyStream(inputStream, outputStream, context)
    }

    companion object {
        private var handler: SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse>

        init {
            try {
                handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(EventSourcingPocApplication::class.java)
                // If you are using HTTP APIs with the version 2.0 of the proxy model, use the getHttpApiV2ProxyHandler
                // method: handler = SpringBootLambdaContainerHandler.getHttpApiV2ProxyHandler(Application.class);
            } catch (e: ContainerInitializationException) {
                // if we fail here. We re-throw the exception to force another cold start
                e.printStackTrace()
                throw RuntimeException("Could not initialize Spring Boot application", e)
            }
        }
    }
}