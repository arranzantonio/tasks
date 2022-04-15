package tasklist

import kotlinx.datetime.*
import com.squareup.moshi.*
import java.io.File

class Task (private var number: Int,
            var text: String,
            var priority: String,
            var date: String,
            var time: String) {

    val dueTag: String

    init {
        val dateList = date.split('-')
        val year = dateList[0].toInt()
        val month = dateList[1].toInt()
        val day = dateList[2].toInt()
        val taskDate = LocalDate(year, month, day)
        val currentDate = Clock.System.now().toLocalDateTime(TimeZone.of("UTC+0")).date
        val numberOfDays = currentDate.daysUntil(taskDate)
        dueTag = when {
            numberOfDays < 0 -> "O"
            numberOfDays == 0 -> "T"
            else -> "I"
        }
    }

    override fun toString(): String {
        return "$number $date $time $priority $dueTag $text"
    }
    private fun priorityColor(priority: String): String {
        return when (priority) {
            "C" -> "\u001B[101m \u001B[0m"
            "H" -> "\u001B[103m \u001B[0m"
            "N" -> "\u001B[102m \u001B[0m"
            else -> "\u001B[104m \u001B[0m"
        }
    }

    private fun dueTagColor(dueTag: String): String {
        return when (dueTag) {
            "I" -> "\u001B[102m \u001B[0m"
            "T" -> "\u001B[103m \u001B[0m"
            else -> "\u001B[101m \u001B[0m"
        }
    }

    private fun trailing(length: Int) {
        val remaining = 44 - length
        for (i in 1..remaining) {
            print(" ")
        }
        println("|")
    }

    private fun processListOfLines(listOfLines: List<String>): List<String> {
        val procesedListOfLines = mutableListOf<String>()
        for (i in  listOfLines) {
            val chunks = i.length / 44
            val remain = i.length % 44
            var control = 0
            for (j in 1..chunks) {
                procesedListOfLines.add(i.substring(control,control + 44))
                control += 44
            }
            if (remain != 0) procesedListOfLines.add(i.substring(control,control + remain))
        }
        return procesedListOfLines
    }

    fun printTask() {
        var listOfLines = text.split("\n")
        listOfLines = processListOfLines(listOfLines)
        if (number < 10) {
            print(
                "| $number  | $date | $time | ${priorityColor(priority)} |" +
                        " ${dueTagColor(dueTag)} |${listOfLines[0]}"
            )
            trailing(listOfLines[0].length)
        }
        else {
            print("| $number | $date | $time | ${priorityColor(priority)} |" +
                    " ${dueTagColor(dueTag)} |${listOfLines[0]}")
            trailing(listOfLines[0].length)
        }

        for (l in 1 .. listOfLines.lastIndex) {
                print("|    |            |       |   |   |${listOfLines[l]}")
                trailing(listOfLines[l].length)
            }
    }

    fun decrementPosition() {
        --number
    }
}

fun menu(): String{
    var action: String
    val actions = arrayOf("add", "print", "edit", "delete", "end")
    do {
        println("Input an action (add, print, edit, delete, end):")
        action = readln().lowercase()
        if (action !in actions) println("The input action is invalid")
    } while(action !in actions)
    return action
}

fun printDashes(n: Int) {
    for (i in 1..n) {
        print('-')
    }
}

fun printLineSeparator() {
    print('+')
    printDashes(4)
    print('+')
    printDashes(12)
    print('+')
    printDashes(7)
    print('+')
    printDashes(3)
    print('+')
    printDashes(3)
    print('+')
    printDashes(44)
    println('+')
}

fun printHeader() {
    println("| N  |    Date    | Time  | P | D |                   Task                     |")
}

fun printTasks(tasks: MutableList<Task>) {
    if (tasks.size == 0)
        println("No tasks have been input")
    else {
        printLineSeparator()
        printHeader()
        printLineSeparator()
        for (t in tasks) {
            t.printTask()
            printLineSeparator()
        }
    }
}

fun getPriority(): String {
        var p: String? = null
        while (p == null) {
            println("Input the task priority (C, H, N, L):")
            p = when (readln().uppercase()) {
                "C" -> "C"
                "H" -> "H"
                "N" -> "N"
                "L" -> "L"
                else -> null
            }
        }
        return p
}

fun getDate(): String {
    var notCorrect = true
    var date = LocalDate(0, 1, 1)

    while (notCorrect) {
        try {
            println("Input the date (yyyy-mm-dd):")
            val d = readln().split('-').map { it.toInt() }
            date = LocalDate(d[0], d[1], d[2])
            notCorrect = false
        } catch (e: IllegalArgumentException) {
            println("The input date is invalid")
        } catch (e: IndexOutOfBoundsException) {
            println("The input date is invalid")
        }
    }
    return date.toString()
}

fun getTime(): String {
    var notCorrect = true
    var dateTime = LocalDateTime(0,1,1,0,0)

    while (notCorrect) {
        try {
            println("Input the time (hh:mm):")
            val d = readln().split(':').map { it.toInt() }
            dateTime = LocalDateTime(0, 1, 1, d[0], d[1])
            notCorrect = false
        }
        catch (e: IllegalArgumentException) {
            println("The input time is invalid")
        }
        catch (e: IndexOutOfBoundsException) {
            println("The input time is invalid")
        }
    }
    return dateTime.toString().split('T')[1]
}


fun deleteTask(tasks: MutableList<Task>): MutableList<Task> {
    if (tasks.size == 0) println("No tasks have been input")
    else {
        printTasks(tasks)
        var notCorrectNumber = true
        while (notCorrectNumber) {
            try {
                println("Input the task number (1-${tasks.size}):")
                val n = readln().toInt()
                tasks.removeAt(n-1)
                for (i in n-1 until  tasks.size) {
                    tasks[i].decrementPosition()
                }
                notCorrectNumber = false
                println("The task is deleted")
            }
            catch (e: Exception) {
                println("Invalid task number")
            }
        }
    }
    return tasks
}

fun editTask(tasks: MutableList<Task>): MutableList<Task> {
    if (tasks.size == 0) println("No tasks have been input")
    else {
        printTasks(tasks)
        var notCorrectNumber = true
        while (notCorrectNumber) {
            try {
                println("Input the task number (1-${tasks.size}):")
                val n = readln().toInt()
                val t = tasks[n-1]
                notCorrectNumber = false
                var notCorrectField = true
                while (notCorrectField) {
                    try {
                        println("Input a field to edit (priority, date, time, task):")
                        when (readln()) {
                            "priority" -> t.priority = getPriority()
                            "date" -> t.date = getDate()
                            "time" -> t.time = getTime()
                            "task" -> {
                                val textTask = getText()
                                if (textTask == "") println("The task is blank")
                                else t.text = textTask
                            }
                            else -> throw Exception()
                        }
                        println("The task is changed")
                        notCorrectField = false
                    }
                    catch (e: Exception) {
                        println("Invalid field")
                    }
                }
            }
            catch (e: Exception) {
                println("Invalid task number")
            }
        }
    }
    return tasks
}

fun getText(): String{
    var textTask = ""
    println("Input a new task (enter a blank line to end):")
    do {
        val line = readln().trim()
        if (line != "") textTask += line + "\n"
    } while (line != "")

    return textTask
}

fun addTask(tasks: MutableList<Task>): MutableList<Task> {
    val priority = getPriority()
    val date = getDate()
    val time = getTime()
    val textTask = getText()
    if (textTask == "") println("The task is blank")
    else tasks.add(Task(tasks.lastIndex + 2,textTask, priority, date, time))
    return tasks
}

fun main() {
    val jsonFile = File("tasklist.json")
    val taks = mutableListOf<Task>()
    val jsonStr: String = if (jsonFile.exists()) jsonFile.readText() else ""
    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val type = Types.newParameterizedType(List::class.java, Task::class.java)
    val tasksListAdapter = moshi.adapter<List<Task>>(type)

    val tasks = if (jsonStr != "") run {
        val newTaskList = tasksListAdapter.fromJson(jsonStr)
        newTaskList!!.toMutableList()
    } else {
        mutableListOf<Task>()
    }

    var action = menu()
    while (action != "end") {
        when (action) {
            "print" -> printTasks(tasks)
            "edit" -> editTask(tasks)
            "delete" -> deleteTask(tasks)
            else -> addTask(tasks)
        }
        action = menu()
    }

    val listTasks = tasks.toList()
    jsonFile.writeText(tasksListAdapter.toJson(tasks))
    println("Tasklist exiting!")
}




