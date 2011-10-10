/*
 * Copyright (C) 2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.spray
package typeconversion

import http._

trait TypeConverter[A, B] extends (A => Either[TypeConversionError, B])

object TypeConverter {
  implicit def fromFunction2Converter[A, B](implicit f: A => B) = {
    new TypeConverter[A, B] {
      def apply(a: A) = {
        try {
          Right(f(a))
        } catch {
          case ex => Left(MalformedContent(ex.toString))
        }
      }
    }
  }

  implicit def fromConverter2OptionConverter[A, B](implicit converter: TypeConverter[A, B]) = {
    new TypeConverter[Option[A], B] {
      def apply(value: Option[A]) = value match {
        case Some(a) => converter(a)
        case None => Left(ContentExpected)
      }
    }
  }
}

sealed trait TypeConversionError
case object ContentExpected extends TypeConversionError
case class MalformedContent(errorMessage: String) extends TypeConversionError
case class UnsupportedContentType(supported: List[ContentTypeRange]) extends TypeConversionError