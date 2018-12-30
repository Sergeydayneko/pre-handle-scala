package com.dayneko.predialValidation.model

/** case classes for specific error codes **/
sealed abstract class LockStatus
case class Lock(responseCode: Int, text: String) extends LockStatus
case class NotLock(key: String, counter: String, duration: Int) extends LockStatus
case class LockServerError(responseCode: Int, text: String) extends LockStatus
