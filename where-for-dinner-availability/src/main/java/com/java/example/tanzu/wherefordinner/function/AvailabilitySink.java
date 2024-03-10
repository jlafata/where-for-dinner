package com.java.example.tanzu.wherefordinner.function;

import java.time.Duration;
import java.util.ArrayList;
import java.util.function.Function;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import org.springframework.retry.*;

import com.java.example.tanzu.wherefordinner.model.Availability;
import com.java.example.tanzu.wherefordinner.model.Availability.AvailabilityWindow;
import com.java.example.tanzu.wherefordinner.model.Search;
import com.java.example.tanzu.wherefordinner.repository.AvailabilityRepository;
import com.java.example.tanzu.wherefordinner.repository.AvailabilityWindowRepository;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Configuration
@Slf4j
public class AvailabilitySink
{
	@Autowired
	protected AvailabilityRepository availRepo;
	
	@Autowired
	protected AvailabilityWindowRepository availWinRepo;

	/*  to address the issue where rabbit queues stop respoding with reactive streams
		2024-02-28T13:29:22.097-05:00 [APP/PROC/WEB/0] [OUT] at java.base/java.lang.Thread.run(Unknown Source)
		2024-02-28T13:29:22.097-05:00 [APP/PROC/WEB/0] [OUT] Caused by: java.lang.IllegalStateException: The [bean 'processAvailability-in-0'] doesn't have subscribers to accept messages

		add retry following this reference:
		https://stackoverflow.com/questions/74423898/consumption-of-events-stopped-after-the-consumer-throw-an-exception-in-spring-cl

		this is the package to include in imports
		https://projectreactor.io/docs/core/3.5.4/api/reactor/util/retry/RetrySpec.html?is-external=true
	 */
	@Bean
	@RegisterReflectionForBinding({Availability.class, AvailabilityWindow.class})
	public Function<Flux<Availability>, Mono<Void>> processAvailability()
	{
		return avails -> avails.flatMap(avail ->
		{
			log.info("Received availability for dining name {} in search {} for subject {}", avail.getDiningName(), avail.getSearchName(), avail.getRequestSubject());

			// check to see if this is an update or a new entry
			return availRepo.findBySearchNameAndDiningNameAndRequestSubject(avail.getSearchName(), avail.getDiningName(), avail.getRequestSubject())
					.switchIfEmpty(Mono.just(new com.java.example.tanzu.wherefordinner.entity.Availability(null, "", "", "", "", "", "", "", "", "")))
					.flatMap(foundAvail ->
					{
						// add a new availability entry if one does not already exist for this search/dining combo
						final Mono<com.java.example.tanzu.wherefordinner.entity.Availability> saveAvail = (foundAvail.getId() != null) ?
								Mono.just(foundAvail) :
								availRepo.save(new com.java.example.tanzu.wherefordinner.entity.Availability(null, avail.getSearchName(), avail.getDiningName(),
										avail.getAddress(), avail.getLocality(), avail.getRegion(), avail.getPostalCode(), avail.getPhoneNumber(),
										avail.getReservationURL(), avail.getRequestSubject()));

						return saveAvail.flatMap(savedAvail ->
						{
							// delete any existing window entries and add new entries
							return availWinRepo.deleteByAvailabilityId(savedAvail.getId())
									.then(saveTimeWindows(avail, savedAvail));

						});

					});
		})
			.retryWhen(reactor.util.retry.Retry.fixedDelay(2, Duration.ofSeconds(3)))
			.then();
	}


    @Bean
	public Function<Flux<Search>, Mono<Void>> processDeletedSearch()
	{
		return searches -> searches.flatMap(search ->
		{
			log.info("Deleting availability in search name \'{}\' for subject \'{}\'", search.getName(), search.getRequestSubject());
			
			return availRepo.findBySearchNameAndRequestSubject(search.getName(), search.getRequestSubject())
			.map(avail -> avail.getId())
			.collectList()
			.flatMap(availIds -> availRepo.deleteAllById(availIds).then(availWinRepo.deleteByAvailabilityIdIn(availIds)));
		}).then();
	}
	
	protected Mono<Void> saveTimeWindows(Availability avail, com.java.example.tanzu.wherefordinner.entity.Availability savedAvail)
	{
		  // no available windows... exit
		  if (avail.getAvailabilityWindows().isEmpty())
			  return Mono.empty();
		  
		  log.info("Saving {} windows for dining name {} in search {}", avail.getAvailabilityWindows().size(), avail.getDiningName(), avail.getSearchName());
		  
		  // save available windows
		  final var saveWindows = new ArrayList<com.java.example.tanzu.wherefordinner.entity.AvailabilityWindow>();
		  avail.getAvailabilityWindows().forEach(win ->  
             saveWindows.add(new com.java.example.tanzu.wherefordinner.entity.AvailabilityWindow(savedAvail.getId(), win.getStartTime(), win.getEndTime())));

		  return availWinRepo.saveAll(saveWindows).then();						  
		  
	}
}
