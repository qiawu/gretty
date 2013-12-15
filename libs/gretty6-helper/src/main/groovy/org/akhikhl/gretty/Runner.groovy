/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonSlurper
import org.mortbay.jetty.security.HashUserRealm
import org.mortbay.jetty.security.SecurityHandler
import org.mortbay.jetty.security.UserRealm
import org.mortbay.jetty.Connector
import org.mortbay.jetty.Server
import org.mortbay.jetty.bio.SocketConnector
import org.mortbay.jetty.webapp.WebAppClassLoader
import org.mortbay.jetty.webapp.WebAppContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class Runner extends RunnerBase {

  private static final Logger log = LoggerFactory.getLogger(Runner)

  static void main(String[] args) {
    if(args.length == 0) {
      log.error 'Arguments to Runner not specified'
      return
    }
    log.trace 'Runner args: {}', args
    Map params = new JsonSlurper().parseText(args[0])
    log.trace 'Runner params: {}', params
    new Runner(params).run()
  }

  private Runner(Map params) {
    super(params)
  }

  protected void configureConnectors() {
    SocketConnector connector = new SocketConnector()
    // Set some timeout options to make debugging easier.
    connector.setMaxIdleTime(1000 * 60 * 60)
    connector.setSoLingerTime(-1)
    connector.setPort(params.port)
    server.setConnectors([ connector ] as Connector[])
  }

  protected void configureRealm(context) {
    Map realmInfo = params.realmInfo
    if(realmInfo?.realm && realmInfo?.realmConfigFile)
      context.getSecurityHandler().setUserRealm(new HashUserRealm(realmInfo.realm, realmInfo.realmConfigFile))
  }

  protected createServer() {
    return new Server()
  }

  protected createWebAppContext(ClassLoader classLoader) {
    WebAppContext context = new WebAppContext()
    context.setClassLoader(new WebAppClassLoader(classLoader, context))
    return context
  }
}
