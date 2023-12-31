package uz.narzullayev.zoom.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.*;

/**
 * Stores a POJO as a Newline-delimitied JSON (http://ndjson.org/)
 * - each version as a new line.
 *
 * @author charles.lobo
 */
@Getter
@Setter
public class SimplePersist {

	private String dbName;
	private boolean hasdata;

	public SimplePersist(String dbName) throws IOException {
		this.dbName = dbName;
		try {
			BufferedReader db = new BufferedReader(new FileReader(dbName));
			hasdata = db.readLine() != null;
			db.close();
		} catch(FileNotFoundException e) {
			hasdata = false;
		}
	}

	/**
	 * Save the given POJO exported as JSON to a new line in our db file
	 */
	public <T> void save(T pojo) throws Exception {
		BufferedWriter db = new BufferedWriter(new FileWriter(dbName,false));
		ObjectMapper mapper = new ObjectMapper();
		String ndjson = mapper.writeValueAsString(pojo);
		if(hasdata) ndjson = "\n" + ndjson;
		db.newLine();
		db.write(ndjson);
		db.flush();
		db.close();
	}

	/**
	 * Load the last saved line from our DB file
	 */
	public String loadLastEntry() throws IOException {
		String line, last = null;
		BufferedReader db = new BufferedReader(new FileReader(dbName));
		while((line  = db.readLine()) != null) {
			last = line;
		}
		db.close();
		return last;
	}

	/**
	 * Load the last saved line from our db file and read it in as a JSON of the
	 * requested POJO class
	 */
	public <T> T load(Class<T> cls) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return mapper.readValue(loadLastEntry(), cls);
	}
}
