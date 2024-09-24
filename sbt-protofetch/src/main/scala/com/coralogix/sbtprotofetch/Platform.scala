/*
 * Copyright 2024 Coralogix Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.coralogix.sbtprotofetch

case class SystemTuple(
    platform: Platform,
    architecture: Architecture
) {
  def cacheKey: String =
    s"${platform.toString.toLowerCase}-${architecture.toString.toLowerCase}"
}

object SystemTuple {
  def detect(): SystemTuple =
    SystemTuple(Platform.detect(), Architecture.detect())
}

sealed trait Platform

object Platform {
  case object Windows extends Platform
  case object Linux   extends Platform
  case object Mac     extends Platform

  def detect(): Platform = {
    System.getProperty("os.name").toLowerCase match {
      case mac if mac.contains("mac")       => Mac
      case win if win.contains("win")       => Windows
      case linux if linux.contains("linux") => Linux
      case osName => scala.sys.error(s"Unknown operating system: $osName")
    }
  }
}

sealed trait Architecture

object Architecture {
  case object Amd64 extends Architecture {
    override def toString: String = "amd64"
  }
  case object AArch64 extends Architecture {
    override def toString: String = "aarch64"
  }

  def detect(): Architecture =
    System.getProperty("os.arch").toLowerCase match {
      case arm if arm.contains("aarch64") => AArch64
      case _                              => Amd64
    }
}
