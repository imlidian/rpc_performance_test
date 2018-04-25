import java.io.{ BufferedReader, File, FileOutputStream, InputStreamReader }
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

import scala.io.Source

object Typ extends Enumeration {
	type Typ = Value

	val Thrpt = Value(0, "Thrpt")
	val Avgt = Value(1, "Avgt")
	val P90 = Value(2, "P90")
	val P99 = Value(3, "P99")
	val P999 = Value(4, "P999")
}

case class Item(task : String, typ : Typ.Typ, fun : String, score : Double)

case class Record(task : String, thrpt : Double, avgt : Double, p90 : Double, p99 : Double, p999 : Double)

object benchmark1 {
	val jvmOps = "java -server -Xmx1g -Xms1g -XX:MaxDirectMemorySize=1g -XX:+UseG1GC"
	val resultFolder = new File("benchmark-result")

	val funOrder = Array("existUser", "createUser", "getUser", "listUser")
	val emptyItem = Item(null, Typ.Thrpt, null, 0D)

	// 主函数	
	def main(args : Array[String]) : Unit = {
		// installBenchmarkBase()

		// val allTasks = getAllTasks()
		// 	.filterNot(_ == "tars")

		//println("找到以下benchmark项目:")
		// println(allTasks.mkString(", "))

		// allTasks.foreach(benchmark(_))

		//benchmark("pojo")

		println("转换log:")

		report()
	}
	// //step 1 执行benchmark-base 下的目录
	// def installBenchmarkBase() {
	// 	exec("benchmark-base", "mvn clean install")
	// }
	// // stepm2 获取全部文件夹
	// def getAllTasks() : Array[String] = {
	// 	val folder = new File(".")

	// 	folder
	// 		.list
	// 		.filter(_.endsWith("-client"))
	// 		.map(name => name.substring(0, name.length() - "-client".length()))
	// 		.sorted
	// }
	//step3 获取的文件名字开始报告
	def benchmark(taskName : String) {
		val serverPackage = packageAndGet(new File("pojo-client-1.0.0-m2-SNAPSHOT"))
		val clientPackage = packageAndGet(new File("pojo-server-1.0.0-m2-SNAPSHOT"))
// val serverPackage = packageAndGet(new File(taskName + "-server"))
// 		val clientPackage = packageAndGet(new File(taskName + "-client"))
// pojo-client-1.0.0-m1.jar
		startServer(serverPackage)

		//等服务器启动起来在启动客户端
		TimeUnit.SECONDS.sleep(5)

		startClient(clientPackage)

		stopServer(serverPackage)
	}

	def packageAndGet(project : File) : File = {
		//exec(project, "mvn clean package")

		val childs = new File(project, "target").listFiles()

		if (childs.find(_.getName.endsWith("-jar-with-dependencies.jar")).isDefined) {
			return childs.find(_.getName.endsWith("-jar-with-dependencies.jar")).get
		}

		return childs.filterNot(_.getName.startsWith("original-")).find(_.getName.endsWith(".jar")).get
	}

	def taskName(pkg : File) = {
		val name = pkg.getName

		if (name.endsWith("-jar-with-dependencies.jar")) {
			name.substring(0, name.length() - "-jar-with-dependencies.jar".length())
		} else {
			name.substring(0, name.length() - ".jar".length())
		}
	}
	//step4 定义log目录 拷贝到服务端 启动服务
	def startServer(serverPackage : File) {
				println(s"start startServer")

		val name = serverPackage.getName
		println(s"start： $name")

		val resultPath = taskName(serverPackage) + ".log"

		//copy到benchmark-server
		//exec(serverPackage.getParentFile, s"scp ${name} root@benchmark-server:~")

		//杀掉benchmark-server上的老进程
		exec(Array("ssh", "root@benchmark-server", "killall java"))

		//benchmark-server上启动服务器
		val remoteCommand = s"nohup ${jvmOps} -jar ${name} >> ${resultPath} &"
		println(s"remoteCommand： $remoteCommand")

		exec(Array("ssh", "root@benchmark-server", remoteCommand))
	}

	def stopServer(serverPackage : File) {
		val name = serverPackage.getName
		println(s"stop $name")

		//benchmark-server上启动服务器
		exec(Array("ssh", "root@benchmark-server", "killall java"))
	}

	def startClient(clientPackage : File) {
		val name = clientPackage.getName
		println(s"start $name")

		val resultFile = new File(resultFolder, taskName(clientPackage) + ".log")

		val command = s"${jvmOps} -jar ${name}"

		//启动客户端
		exec(clientPackage.getParentFile, command, resultFile)
	}

	// cmd执行
	def exec(path : String, command : String) {
		if (path != null) exec(new File(path), command)
		else exec(null.asInstanceOf[File], command)
	}

	def exec(command : String) {
		exec(null.asInstanceOf[File], command)
	}

	def exec(file : File, command : String, redirect : File = null) {
		val process = Runtime.getRuntime().exec(command, null, file)

		val inputStream = process.getInputStream()
		val inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
		val reader = new BufferedReader(inputStreamReader)

		if (redirect != null && !redirect.exists()) {
			redirect.getParentFile.mkdirs();
		}

		val output : FileOutputStream =
			if (redirect != null) new FileOutputStream(redirect) else null

		try {
			var line = ""
			while ({ line = reader.readLine(); line != null }) {
				println(line)

				if (output != null) {
					output.write(line.getBytes("UTF-8"))
					output.write("\r\n".getBytes("UTF-8"))
				}
			}
		} catch {
			case t : Throwable => t.printStackTrace()
		} finally {
			reader.close()
			inputStreamReader.close()
			inputStream.close()

			if (output != null) {
				output.flush()
				output.close()
			}

		}

		process.waitFor();
		process.destroy();
	}

	def exec(commands : Array[String]) {
		val process = Runtime.getRuntime().exec(commands)

		val inputStream = process.getInputStream()
		val inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)
		val reader = new BufferedReader(inputStreamReader)

		try {
			var line = ""
			while ({ line = reader.readLine(); line != null }) {
				println(line)
			}
		} catch {
			case t : Throwable => t.printStackTrace()
		} finally {
			reader.close()
			inputStreamReader.close()
			inputStream.close()
		}

		process.waitFor();
		process.destroy();
	}

	// 报告执行
	def report() {
		val reportFile = new File(resultFolder, "benchmark-report.md")
		val reportOutput = new FileOutputStream(reportFile)
		val props = sys.props
		println(resultFolder)
		reportOutput.write(s"#RPC性能报告\r\n".getBytes("UTF-8"))
		reportOutput.write(s"> 生成时间: ${LocalDateTime.now()}<br>\r\n".getBytes("UTF-8"))
		reportOutput.write(s"> 运行环境: ${props("os.name")}, ${props("java.vm.name")} ${props("java.runtime.version")}<br>\r\n".getBytes("UTF-8"))
		reportOutput.write(s"> 启动参数: ${jvmOps}<br>\r\n".getBytes("UTF-8"))

		reportOutput.write(s"\r\n".getBytes("UTF-8"))

		val header = """| framework | thrpt (ops/ms) | avgt (ms) | p90 (ms) | p99 (ms) | p999 (ms) |
				\| :--- | :---: | :---: | :---: | :---: | :---: |
				\""".stripMargin('\\')

		resultFolder
			.listFiles()
			.filter(_.getName().endsWith(".log"))
			.filter(_.getName().contains("-client-"))
			.flatMap(resultFile => {
				val name = resultFile.getName
				val index = name.indexOf("-client-")
				val task = name.substring(0, index)

				Source
					.fromFile(resultFile)
					.getLines()
					.filter(_.startsWith("Client."))
					.map(extract(task, _))
					.filter(_ != null)
			})
			.groupBy(_.fun)
			.toList
			.sortBy(kv => funOrder.indexOf(kv._1))
			.foreach(kv => {
				val fun = kv._1
				val items = kv._2
				val records = toRecords(items)

				reportOutput.write(s"\r\n##${fun}\r\n".getBytes("UTF-8"))
				reportOutput.write(header.getBytes("UTF-8"))

				records.foreach(record => {
					val line = s"|${record.task}|${record.thrpt}|${record.avgt}|${record.p90}|${record.p99}|${record.p999}|\r\n"
					reportOutput.write(line.getBytes("UTF-8"))
				})

				reportOutput.write(s"\r\n".getBytes("UTF-8"))
			})

		reportOutput.flush()
		reportOutput.close()

		println(s"成功生成性能报告: ${reportFile.getAbsolutePath}")
	}

	def toRecords(items : Array[Item]) = {
		import Typ._

		items
			.groupBy(_.task)
			.values
			.map(list => {
				val task = list.head.task

				val thrpt = list.find(_.typ == Thrpt).getOrElse(emptyItem).score
				val avgt = list.find(_.typ == Avgt).getOrElse(emptyItem).score
				val p90 = list.find(_.typ == P90).getOrElse(emptyItem).score
				val p99 = list.find(_.typ == P99).getOrElse(emptyItem).score
				val p999 = list.find(_.typ == P999).getOrElse(emptyItem).score

				Record(task, thrpt, avgt, p90, p99, p999)
			})
			.toList
			.sortBy(-_.thrpt)
	}

	def extract(task : String, line : String) : Item = {
		if (line == null || line.length() == 0) {
			return null
		}

		import Typ._

		if (line.contains(" thrpt ")) {
			val array = line.split("\\s+")

			val fun = array(0).replace("Client.", "")
			val score = array(3).toDouble

			return Item(task, Thrpt, fun, score)
		}

		if (line.contains(" avgt ")) {
			val array = line.split("\\s+")

			val fun = array(0).replace("Client.", "")
			val score = array(3).toDouble

			return Item(task, Avgt, fun, score)
		}

		if (line.contains("·p0.90 ")) {
			val array = line.split("\\s+")

			var fun = array(0)
			val begin = fun.indexOf(':') + 1
			val end = fun.indexOf('·')
			fun = fun.substring(begin, end)

			val score = array(2).toDouble

			return Item(task, P90, fun, score)
		}

		if (line.contains("·p0.99 ")) {
			val array = line.split("\\s+")

			var fun = array(0)
			val begin = fun.indexOf(':') + 1
			val end = fun.indexOf('·')
			fun = fun.substring(begin, end)

			val score = array(2).toDouble

			return Item(task, P99, fun, score)
		}

		if (line.contains("·p0.999 ")) {
			val array = line.split("\\s+")

			var fun = array(0)
			val begin = fun.indexOf(':') + 1
			val end = fun.indexOf('·')
			fun = fun.substring(begin, end)

			val score = array(2).toDouble

			return Item(task, P999, fun, score)
		}

		return null
	}

}
