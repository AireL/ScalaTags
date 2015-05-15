package com.aire

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.annotation.compileTimeOnly

object Tags {
  @compileTimeOnly("enable macro paradise to expand macro annotations")
  class tag(name: String *)  extends StaticAnnotation {
    def macroTransform(annottees : Any*) : Any = macro tag.impl
  }

  object tag {
    def impl(c : Context)(annottees : c.Expr[Any]*) : c.Expr[Any] = {
      import c.universe._
      val inputs = annottees.map(_.tree).toList
      val (param, clazz) = inputs match {
        case List(param : ValDef, q"case class $className(..$fields) extends ..$bases { ..$body }") => (param, (className, fields, bases, body))
        case _ => c.abort(c.enclosingPosition, "[Typed] Invalid annottee. This annotation can only be placed on the fields of a class")
      }
      try {
        val q"$mods val $name: $tpt = $rhs" = param
        val (traitName, traitNameTag) = c.prefix.tree match {
          case q"new $tag($param)" => param match {
            case Literal(Constant(field: String)) => (TypeName(field), TypeName(field + "Tag"))
            case other => c.abort(c.enclosingPosition, s"[Typed] Match error with $other")
          }
          case q"new $tag()" => val capitalName = capitalise(name.decodedName.toString)
            (TypeName(capitalName), TypeName(capitalName + "Tag"))
          case _ => c.abort(c.enclosingPosition, "[Typed] Invalid annotation. This annotation can only take one parameter or less")
        }

        val tup = clazz._2.span { paramdef =>
          val q"$modsA val $nameA: $tptA = $rhsA" = paramdef
          nameA != name
        }
        val newFields = tup._1 ++ (tup._2 match {
          case head :: tail =>
            val q"$mods val $name: $tpt = $rhs" = head
            val valdef = q"$mods val $name: $tpt @@ ${clazz._1}#$traitName = $rhs"
            List(valdef) ++ tail
          case Nil => c.abort(c.enclosingPosition, "[Typed] Unexpected exception. Attempted to append the new value but could not split on it.")
        })
        c.Expr(q"""
        case class ${clazz._1}(${newFields : _*}) extends ..${clazz._3} {
           trait $traitName
           type $traitNameTag = $tpt @@ $traitName
           ..${clazz._4}
         }
        """)
      } catch {
        case _ : MatchError => c.abort(c.enclosingPosition, "[Typed] Invalid annottee. This annotation can only be placed on a val")
      }
    }

    private def capitalise(s : String) = s(0).toUpper + s.substring(1, s.length)
  }

  // Shamelessly lifted from Scalaz. Thanks Miles Sabin and Jason Zaugg!
  type Tagged[U] = { type Tag = U }
  type @@[T, U] = T with Tagged[U]

  implicit class tagApplication[T](val tag : T) extends AnyVal {
    def as[A] : T @@ A = tag.asInstanceOf[T @@ A]
  }
  implicit class appliedTag[A, T](val tagged : A @@ T) extends AnyVal {
    implicit def unwrap : A = tagged.asInstanceOf[A]
  }
}
