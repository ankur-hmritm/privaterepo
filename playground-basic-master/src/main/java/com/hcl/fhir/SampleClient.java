package com.hcl.fhir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.AdditionalRequestHeadersInterceptor;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

public class SampleClient {

	private static FhirContext fhirContext;
	private static IParser parser;
	static double totalElapsedTime = 0;

	public static void main(String[] theArgs) throws IOException {

		fhirContext = FhirContext.forR4();
		parser = fhirContext.newJsonParser();
		
		SampleClient sampleClient = new SampleClient();
		
		// Create a FHIR client
		IGenericClient client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
		client.registerInterceptor(new LoggingInterceptor(false));

		AdditionalRequestHeadersInterceptor additionalHttpHeadersInterceptor =
                new AdditionalRequestHeadersInterceptor();
        additionalHttpHeadersInterceptor.addHeaderValue("Cache-Control", "nocache");
        
		IntStream.rangeClosed(1, 3)
		.forEach(i -> {
			System.out.println("Iterating through Loop:: " + i);
			try {
				if(i == 3){
					client.registerInterceptor(additionalHttpHeadersInterceptor);
				}
				sampleClient.readLastNamesFromInputStream().stream().forEach(lastName -> {
					// Search for Patient resources
					long start = System.currentTimeMillis();
					Bundle response = client.search().forResource("Patient").where(Patient.FAMILY.matches().value(lastName))
							.returnBundle(Bundle.class).execute();
					long elapsedTime = ((System.currentTimeMillis() - start) / 1000) % 60;
					
					totalElapsedTime = totalElapsedTime + elapsedTime;
				    
					response.getEntry().stream()
					.filter(bundleEntryComponent -> (bundleEntryComponent.getResource() instanceof Patient))
					.map(SampleClient::getMappedPatient)
					.sorted(Comparator.comparing(com.hcl.fhir.Patient::getFirstName)).collect(Collectors.toList())
					.forEach(patient -> {
						System.out.println("Patient First Name: " + patient.getFirstName() + "||Patient Last Name: "
								+ patient.getLastName() + "||Patient Date of Birth: " + patient.getDob());
					});
					
				});
				System.out.println("Loop " + i + " Average Time:: " + totalElapsedTime/10);
				totalElapsedTime = 0;
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	private List<String> readLastNamesFromInputStream() throws IOException {
		List<String> lastNames = new ArrayList<String>();
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream("patient-last-name.txt");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = br.readLine()) != null) {
				lastNames.add(line);
			}
		}
		return lastNames;
	}
	
	private static com.hcl.fhir.Patient getMappedPatient(BundleEntryComponent bundleEntryComponent){
		return new com.hcl.fhir.Patient(
				parser.parseResource(Patient.class,
						fhirContext.newJsonParser().setPrettyPrint(true)
								.encodeResourceToString(bundleEntryComponent.getResource()))
				.getName().get(0).getGivenAsSingleString(),
				parser.parseResource(Patient.class,
								fhirContext.newJsonParser().setPrettyPrint(true)
										.encodeResourceToString(bundleEntryComponent.getResource()))
						.getName().get(0).getFamily(),
				Optional.ofNullable(parser.parseResource(Patient.class,
						fhirContext.newJsonParser().setPrettyPrint(true)
								.encodeResourceToString(bundleEntryComponent.getResource()))
						.getBirthDate()).orElse(null));
	}

}
