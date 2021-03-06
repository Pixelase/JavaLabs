package multithreading;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.sql.SQLException;

import main.ILogParser;
import main.LogEntry;
import main.LogParser;
import database.DatabaseWorker;

class Consumer implements Runnable {

	private ILogParser parser;
	private static volatile PrintWriter out;
	private static volatile int count = 0;
	private final int rowsToRead;
	private BlockingQueue<String> linesQueue;
	private BlockingQueue<LogEntry> entriesQueue;

	Consumer(String path, int amountOfLines, BlockingQueue<String> queueLines,
			BlockingQueue<LogEntry> queueLogs) throws IOException {
		parser = new LogParser();
		this.rowsToRead = amountOfLines;
		this.linesQueue = queueLines;
		this.entriesQueue = queueLogs;
		out = new PrintWriter(new BufferedWriter(new FileWriter(path)));
	}

	@Override
	public void run() {

		try {
			while (count < rowsToRead) {

				consume(linesQueue.take());
			}
			out.close();
			DatabaseWorker.closeDatabase();
		} catch (InterruptedException | SQLException | ClassNotFoundException ex) {
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void consume(String line) throws InterruptedException,
			UnknownHostException, SQLException {
		++count;
		main.LogEntry entry = parser.parse(line);
		// out.println(entry);
		// DatabaseConnect.WriteDatabase(entry, count);
		if (ConsumerReport.runnable) {
			entriesQueue.put(entry);
		}
	}
}