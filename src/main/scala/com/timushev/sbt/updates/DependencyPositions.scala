package com.timushev.sbt.updates

import sbt.Keys._
import sbt.{given, _}

import scala.util.control.NonFatal

object DependencyPositions {

  def dependencyPositionsTask: Def.Initialize[Task[Map[ModuleID, Set[SourcePosition]]]] =
    Def.task {
      try {
        val projRef             = thisProjectRef.value
        val st                  = state.value
        val extracted           = Project.extract(st)
        val empty: Def.Settings = Def.empty(using s => Seq(s))
        val settings = extracted.structure.settings.filter { s =>
          (s.key.key == libraryDependencies.key) && (s.key.scope.project == Select(projRef))
        }
        settings
          .flatMap { case s: Setting[Seq[ModuleID]] @unchecked =>
            s.init.evaluate(empty).map(_ -> s.pos)
          }
          .groupBy(_._1)
          .map { case (k, v) => k -> v.map(_._2).toSet }
      } catch {
        case NonFatal(_) =>
          dependencyPositions.value
            .map { case (k, v) => k -> Set(v) }
      }
    }

}
