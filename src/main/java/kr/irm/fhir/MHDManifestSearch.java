package kr.irm.fhir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import kr.irm.fhir.util.MyResponseHandler;
import kr.irm.fhir.util.URIBuilder;
import org.apache.commons.cli.*;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MHDManifestSearch extends UtilContext {
	private static final Logger LOG = LoggerFactory.getLogger(MHDManifestSearch.class);

	public static void main(String[] args) {
		LOG.info("starting mhd manifest search...");
		LOG.info("option args:{} ", Arrays.toString(args));
		Options opts = new Options();
		Map<String, Object> optionMap = new HashMap<String, Object>();
		setOptions(opts);

		// parse options
		if (parseOptions(optionMap, opts, args)) {
			LOG.error("mhd manifest search failed: invalid options");
			System.exit(1);
		}

		doSearch(optionMap);
	}

	private static void setOptions(Options opts) {
		// help
		opts.addOption("h", "help", false, "help");

		// Commons
		opts.addOption("o", OPTION_OAUTH_TOKEN, true, "OAuth Token");
		opts.addOption("s", OPTION_SERVER_URL, true, "FHIR Server Base URL");
		opts.addOption("pu", OPTION_PATIENT_UUID, true, "Patient.id (UUID)");
		opts.addOption("mu", OPTION_MANIFEST_UUID, true, "DocumentManifest.id (UUID)");
		opts.addOption("i", OPTION_ID, true, "id");
		opts.addOption("pi", OPTION_PATIENT_IDENTIFIER, true, "patient.identifier");
		opts.addOption("c", OPTION_CREATED, true, "created");
		opts.addOption("af", OPTION_AUTHOR_FAMILY, true, "author.family");
		opts.addOption("ag", OPTION_AUTHOR_GIVEN, true, "author.given");
		opts.addOption("id", OPTION_IDENTIFIER, true, "identifier");
		opts.addOption("t", OPTION_TYPE, true, "type");
		opts.addOption("sc", OPTION_SOURCE, true, "source");
		opts.addOption("st", OPTION_STATUS, true, "status");
		opts.addOption("sr", OPTION_SORT, true, "sort");
		opts.addOption("of", OPTION_OFFSET, true, "offset");
		opts.addOption("co", OPTION_COUNT, true, "count");
		opts.addOption("f", OPTION_FORMAT, true, "Response Format (application/fhir+json or application/fhir+xml)");
	}

	private static boolean parseOptions(Map<String, Object> optionMap, Options opts, String[] args) {
		boolean error = false;
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cl = parser.parse(opts, args);

			// HELP
			if (cl.hasOption("h") || args.length == 0) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(
						"MHDManifestSearch.sh [options]",
						"\nSearch Document Manifest from MHD DocumentRecipient", opts,
						"Examples: $ ./MHDManifestSearch.sh --document-status ...");
				System.exit(2);
			}

			// OAuth token (Required)
			if (cl.hasOption(OPTION_OAUTH_TOKEN)) {
				String oauth_token = cl.getOptionValue(OPTION_OAUTH_TOKEN);
				LOG.info("option {}={}", OPTION_OAUTH_TOKEN, oauth_token);

				optionMap.put(OPTION_OAUTH_TOKEN, oauth_token);
			}

			// FHIR
			// Server-url (Required)
			if (cl.hasOption(OPTION_SERVER_URL)) {
				String server_url = cl.getOptionValue(OPTION_SERVER_URL);
				LOG.info("option {}={}", OPTION_SERVER_URL, server_url);

				optionMap.put(OPTION_SERVER_URL, server_url);
			} else {
				error = true;
				LOG.error("option required: {}", OPTION_SERVER_URL);
			}

			// Manifest ResourceId (UUID for Lookup)
			if (cl.hasOption(OPTION_MANIFEST_UUID)) {
				String manifestUuid = cl.getOptionValue(OPTION_MANIFEST_UUID);
				LOG.info("option {}={}", OPTION_MANIFEST_UUID, manifestUuid);

				optionMap.put(OPTION_MANIFEST_UUID, manifestUuid);
			}

			// id (Document Manifest UUID for Search)
			if (cl.hasOption(OPTION_ID)) {
				String id = cl.getOptionValue(OPTION_ID);
				LOG.info("option {}={}", OPTION_ID, id);

				optionMap.put(OPTION_ID, id);
			}

			// Patient UUID
			if (cl.hasOption(OPTION_PATIENT_UUID)) {
				String patientUuid = cl.getOptionValue(OPTION_PATIENT_UUID);
				LOG.info("option {}={}", OPTION_PATIENT_UUID, patientUuid);

				optionMap.put(OPTION_PATIENT_UUID, patientUuid);
			}

			// patient.identifier (ex. PatientIdValue^^^&AssignerId&AssignerIdType)
			if (cl.hasOption(OPTION_PATIENT_IDENTIFIER)) {
				String patientIdentifier = cl.getOptionValue(OPTION_PATIENT_IDENTIFIER);
				LOG.info("option {}={}", OPTION_PATIENT_IDENTIFIER, patientIdentifier);

				optionMap.put(OPTION_PATIENT_IDENTIFIER, patientIdentifier);
			}

			// created (When this document manifest created)
			if (cl.hasOption(OPTION_CREATED)) {
				String[] component = cl.getOptionValue(OPTION_CREATED).split(",");
				List<String> createdList = getComponentList(component);
				LOG.info("option {}={}", OPTION_CREATED, createdList);

				optionMap.put(OPTION_CREATED, createdList);
			}

			// author.family (Who and/or what authored the DocumentManifest)
			if (cl.hasOption(OPTION_AUTHOR_FAMILY)) {
				String[] component = cl.getOptionValue(OPTION_AUTHOR_FAMILY).split(",");
				List<String> authorFamilyList = getComponentList(component);
				LOG.info("option {}={}", OPTION_AUTHOR_FAMILY, authorFamilyList);

				optionMap.put(OPTION_AUTHOR_FAMILY, authorFamilyList);
			}

			// author.given (Who and/or what authored the DocumentManifest)
			if (cl.hasOption(OPTION_AUTHOR_GIVEN)) {
				String[] component = cl.getOptionValue(OPTION_AUTHOR_GIVEN).split(",");
				List<String> authorGivenList = getComponentList(component);
				LOG.info("option {}={}", OPTION_AUTHOR_GIVEN, authorGivenList);

				optionMap.put(OPTION_AUTHOR_GIVEN, authorGivenList);
			}

			// identifier (Other identifiers for the manifest)
			if (cl.hasOption(OPTION_IDENTIFIER)) {
				String[] component = cl.getOptionValue(OPTION_IDENTIFIER).split(",");
				List<String> identifierList = getComponentList(component);
				LOG.info("option {}={}", OPTION_IDENTIFIER, identifierList);

				optionMap.put(OPTION_IDENTIFIER, identifierList);
			}

			// type (Kind of document set)
			if (cl.hasOption(OPTION_TYPE)) {
				String[] component = cl.getOptionValue(OPTION_TYPE).split(",");
				List<String> typeList = getComponentList(component);
				LOG.info("option {}={}", OPTION_TYPE, typeList);

				optionMap.put(OPTION_TYPE, typeList);
			}

			// source (The source system/application/software)
			if (cl.hasOption(OPTION_SOURCE)) {
				String[] component = cl.getOptionValue(OPTION_SOURCE).split(",");
				List<String> sourceList = getComponentList(component);
				LOG.info("option {}={}", OPTION_SOURCE, sourceList);

				optionMap.put(OPTION_SOURCE, sourceList);
			}

			// status (Document Manifest Status : current | superseded | entered-in-error)
			if (cl.hasOption(OPTION_STATUS)) {
				String[] component = cl.getOptionValue(OPTION_STATUS).split(",");
				List<String> statusList = getComponentList(component);
				LOG.info("option {}={}", OPTION_STATUS, statusList);

				optionMap.put(OPTION_STATUS, statusList);
			}

			// sort
			if (cl.hasOption(OPTION_SORT)) {
				String sort = cl.getOptionValue(OPTION_SORT);
				LOG.info("option {}={}", OPTION_SORT, sort);

				optionMap.put(OPTION_SORT, sort);
			}

			// offset
			if (cl.hasOption(OPTION_OFFSET)) {
				String offset = cl.getOptionValue(OPTION_OFFSET);
				LOG.info("option {}={}", OPTION_OFFSET, offset);

				optionMap.put(OPTION_OFFSET, offset);
			}

			// count
			if (cl.hasOption(OPTION_COUNT)) {
				String count = cl.getOptionValue(OPTION_COUNT);
				LOG.info("option {}={}", OPTION_COUNT, count);

				optionMap.put(OPTION_COUNT, count);
			}

			// format
			if (cl.hasOption(OPTION_FORMAT)) {
				String format = cl.getOptionValue(OPTION_FORMAT);
				LOG.info("option {}={}", OPTION_FORMAT, format);

				optionMap.put(OPTION_FORMAT, format);
			} else {
				String format = "application/fhir+json";
				optionMap.put(OPTION_FORMAT, format);
				LOG.info("option {}={}", OPTION_FORMAT, format);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return error;
	}

	private static String doSearch(Map<String, Object> optionMap) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String httpResult = "";
		try {
			URIBuilder uriBuilder = new URIBuilder((String) optionMap.get(OPTION_SERVER_URL));
			uriBuilder.addPath("DocumentManifest");

			if (optionMap.containsKey(OPTION_MANIFEST_UUID)) {
				uriBuilder.addPath((String) optionMap.get(OPTION_MANIFEST_UUID));
				uriBuilder.addParameter(OPTION_FORMAT, (String) optionMap.get(OPTION_FORMAT));
			} else {
				for (String key : optionMap.keySet()) {
					if (key != OPTION_OAUTH_TOKEN && key != OPTION_SERVER_URL && key != OPTION_MANIFEST_UUID) {
						if (optionMap.get(key) instanceof String) {
							uriBuilder.addParameter(key, (String) optionMap.get(key));
						} else if (optionMap.get(key) instanceof List) {
							for (String s : (List<String>) optionMap.get(key)) {
								uriBuilder.addParameter(key, s);
							}
						}
					}
				}
			}

			String searchUrl = uriBuilder.build().toString();
			LOG.info("search url : {}", searchUrl);

			HttpGet httpGet = new HttpGet(searchUrl);
			httpGet.setHeader("Authorization", "Bearer " + optionMap.get(OPTION_OAUTH_TOKEN));

			ResponseHandler<String> responseHandler = new MyResponseHandler();
			httpResult = httpClient.execute(httpGet, responseHandler);
			LOG.info("Response : \n{}", httpResult);
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		} finally {
			try {
				if (httpClient != null) httpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return httpResult;
	}

	private static List<String> getComponentList(String[] component) {
		List<String> componentList = new ArrayList<>();
		for (String s : component) {
			componentList.add(s);
		}

		return componentList;
	}
}
