

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class for manipulations with files.
 * @author niri
 */
public class TextFileManipulation
{
	
	/**
	 * Rewrites a file with a string.
	 * @param filePath - Path to the file to be written.
	 * @param text - String to be written to the file.
	 * @throws IOException if the named file exists but is a directory rather than a regular file,
	 * does not exist but cannot be created, or cannot be opened for any other reason.
	 */
	public static void writeFile(String filePath, String text) throws IOException
	{
		makeFolder(filePath);
		FileWriter fileWriter = new FileWriter(filePath);
		fileWriter.write(text);
		fileWriter.close();
	}

	/**
	 * Rewrites a file with a string, using the specified encoding.
	 * @param filePath - Path to the file to be written.
	 * @param text - String to be written to the file.
	 * @param encoding - The encoding to be used.
	 * @throws IOException if the named file exists but is a directory rather than a regular file,
	 * does not exist but cannot be created, or cannot be opened for any other reason.
	 */
//	public static void writeFile(String filePath, String text, String encoding) throws IOException
//	{
//		makeFolder(filePath);
//		FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(filePath, encoding);
//		fileWriter.write(text);
//		fileWriter.close();
//	}

	private static void makeFolder(String filePath)
	{
		File file = new File(filePath).getAbsoluteFile().getParentFile();
		if(!file.exists()) file.mkdirs();
	}
	
	/**
	 * Appends a string to a file.
	 * @param filePath - Path to the file to be written to.
	 * @param text - String to be appended to the file.
	 * @throws IOException if the named file exists but is a directory rather than a regular file,
	 * does not exist but cannot be created, or cannot be opened for any other reason.
	 */
	public static void appendFile(String filePath, String text) throws IOException
	{
		//makeFolder(filePath);
		FileWriter fileWriter = new FileWriter(filePath, true);
		fileWriter.write(text);
		fileWriter.close();
	}
	
	/**
	 * Appends a string to a file, using the specified encoding.
	 * @param filePath - Path to the file to be written.
	 * @param text - String to be written to the file.
	 * @param encoding - The encoding to be used.
	 * @throws IOException if the named file exists but is a directory rather than a regular file,
	 * does not exist but cannot be created, or cannot be opened for any other reason.
	 */
//	public static void appendFile(String filePath, String text, String encoding) throws IOException
//	{
//		makeFolder(filePath);
//		FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(filePath, encoding, true);
//		fileWriter.write(text);
//		fileWriter.close();
//	}

	/**
	 * Reads the contents of a Web page into a string.
	 * @param url - url of the Web page to be read
	 * @return - Content of the Web page as a string
	 * @throws IOException if an I/O exception occurs.
	 * @throws MalformedURLException if no protocol is specified, or an unknown protocol is found, or url is null.
	 */
	public static String readWebPage(String url) throws MalformedURLException, IOException
	{
		return readStream(new URL(url).openStream());
	}

	/**
	 * Reads the contents of a Web page into a string, using the specified encoding.
	 * @param url - url of the Web page to be read
	 * @param encoding - The encoding to be used.
	 * @return - Content of the Web page as a string
	 * @throws IOException if an I/O exception occurs.
	 * @throws MalformedURLException if no protocol is specified, or an unknown protocol is found, or url is null.
	 */
	public static String readWebPage(String url, String encoding) throws MalformedURLException, IOException
	{
		return readStream(new URL(url).openStream(), encoding);
	}
	
	/**
	 * Reads the contents of a file into a string.
	 * @param filePath - Path to the file to be read.
	 * @return A string containing the text from the file.
	 * @throws IOException if the file does not exist or cannot be read.
	 */
	public static String readFile(String filePath) throws IOException
	{
		return readStream(new FileInputStream(new File(filePath)));
	}
	
	/**
	 * Reads the contents of a file into a string, using the specified encoding.
	 * @param filePath - Path to the file to be read.
	 * @param encoding - The encoding to be used.
	 * @return A string containing the text from the file.
	 * @throws IOException if the file does not exist or cannot be read.
	 */
	public static String readFile(String filePath, String encoding) throws IOException
	{
		return readStream(new FileInputStream(new File(filePath)), encoding);
	}
	
	public static String readStream(InputStream inputStream) throws IOException
	{
		return read(new BufferedReader(new InputStreamReader(inputStream)));
	}
	
	public static String readStream(InputStream inputStream, String encoding) throws IOException
	{
		return read(new BufferedReader(new InputStreamReader(inputStream, encoding)));
	}
	
	private static String read(BufferedReader bufferedReader) throws IOException
	{
		StringBuilder textBuilder = new StringBuilder();
		String nextLine = bufferedReader.readLine();
		while (nextLine != null) {
			textBuilder.append(nextLine);
			nextLine = bufferedReader.readLine();
			if (nextLine != null) {
				textBuilder.append("\n");
			}
		}
		return textBuilder.toString();
	}
}