/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

abstract class RunnerBase {

  protected final Map params
  protected server

  RunnerBase(Map params) {
    this.params = params
  }

  protected abstract void configureConnectors()

  protected abstract void configureRealm(context)

  protected abstract createServer()

  protected abstract createWebAppContext(ClassLoader classLoader)

  final void run() {

    startServer()

    Thread monitor = new MonitorThread(this)
    monitor.start()

    System.out.println 'Jetty server started.'
    System.out.println 'You can see web-application in browser under the address:'
    System.out.println "http://localhost:${params.port}${params.contextPath}"

    if(params.interactive)
      System.out.println 'Press any key to stop the jetty server.'
    else
      System.out.println 'Enter \'gradle jettyStop\' to stop the jetty server.'
    System.out.println()

    if(params.interactive) {
      System.in.read()
      if(monitor.running)
        ServiceControl.send(params.servicePort, 'stop')
    }

    monitor.join()

    System.out.println 'Jetty server stopped.'
  }

  final void startServer() {
    assert server == null

    ClassLoader classLoader = new URLClassLoader(params.projectClassPath.collect { new URL(it) } as URL[], this.getClass().getClassLoader())

    server = createServer()
    configureConnectors()

    def context = createWebAppContext(classLoader)

    configureRealm(context)

    context.setContextPath(params.contextPath)

    params.initParams?.each { key, value ->
      context.setInitParameter(key, value)
    }

    if(params.inplace)
      context.setResourceBase(params.resourceBase)
    else
      context.setWar(params.resourceBase)

    context.setServer(server)
    server.setHandler(context)

    server.start()
  }

  final void stopServer() {
    server.stop()
    server = null
  }
}
