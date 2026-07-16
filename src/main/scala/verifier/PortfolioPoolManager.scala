package viper.silicon.verifier

import viper.silicon.decider.SMTLib2PreambleReader
import viper.silicon.interfaces.decider.{Prover, Sat, Unknown, Unsat}
import viper.silicon.state.terms.{Not, Term}
import viper.silver.components.StatefulComponent

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{Callable, ConcurrentHashMap, ExecutorService, Executors, Future, TimeUnit}

class PortfolioPoolManager extends StatefulComponent {
  private val numberOfWorkers: Int = Verifier.config.numberOfParallelVerifiers()
  private var threadPool: ExecutorService  = _
  private val preambleReader = new SMTLib2PreambleReader
  private val provenance = new ConcurrentHashMap[String, AtomicInteger]()

  override def reset(): Unit = {
    // Should not need to do anything since the Decider should kill all SMT processes which should stop all running jobs
  }

  override def start(): Unit =  {
    threadPool = Executors.newFixedThreadPool(10)
  }

  override def stop(): Unit =  {
    threadPool.shutdown()
    threadPool.awaitTermination(10, TimeUnit.SECONDS)
    provenance.entrySet().forEach { e => println(s"${e.getKey}: ${e.getValue}")}
  }

  def attemptProof(t: Term, timeout: Option[Int], prover: Prover, preamble: String, strategy: Option[String], log: Iterable[String], unknowns: AtomicInteger): Callable[Boolean] = { () =>
      prover.start(Verifier.config.proverArgs)
      prover.comment(s"\n; $preamble")
      preambleReader.emitPreamble(preamble, prover, true)

      val smt2ConfigOptions =
        Verifier.config.proverConfigArgs().map { case (k, v) => s"(set-option :$k $v)" }

      if (smt2ConfigOptions.nonEmpty && prover.name != "cvc5") {
        preambleReader.emitPreamble(smt2ConfigOptions, prover, true)
      }

      prover.comment("\n; /preamble.smt2")
      preambleReader.emitPreamble("/preamble.smt2", prover, false)

      prover.emit(log)

      if (Thread.interrupted()) { false } else {
        prover.assume(Not(t))
        prover.check(timeout, strategy) match {
          case Sat =>
            prover.stop()
            false
          case Unsat =>
            provenance.putIfAbsent(prover.unique(), new AtomicInteger(0))
            provenance.get(prover.unique()).incrementAndGet();
            prover.stop()
            true
          case Unknown =>
            prover.stop()
            if (unknowns.decrementAndGet() != 0)  {
              while (unknowns.get() != 0 && !Thread.interrupted()) {
                Thread.onSpinWait()
                Thread.sleep(10)
              }
            }
            false
        }
      }
  }

  def attemptProofs(t: Term, timeout: Option[Int], provers: Seq[Prover], preambles: Seq[String], strategies: Seq[Option[String]], log: Iterable[String]): Boolean = {
    import scala.jdk.CollectionConverters._
    assert(provers.length == preambles.length)
    assert(provers.length == strategies.length)
    val unknowns = new AtomicInteger(provers.length)
    val tasks = provers.zip(preambles).zip(strategies).map { case ((prover, preamble), strategy) => attemptProof(t, timeout, prover, preamble, strategy, log, unknowns)}
    val result = threadPool.invokeAny(tasks.asJava)
    provers.foreach(_.stop())
    result
  }
}
