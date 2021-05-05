package io.dsub.discogsdata.batch;

import io.dsub.discogsdata.batch.job.DiscogsJobParametersConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscogsBatchRunner implements ApplicationRunner {

    private final DiscogsJobParametersConverter converter = new DiscogsJobParametersConverter();
    private final JobLauncher jobLauncher;
//    private final JobCreator jobCreator;
//    private final SimpleDiscogsBatchJobParameterResolver
//   DiscogsJobParameterConverter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JobParameters params = makeJobParameters(args);
        System.out.println(params);
        System.out.println(params.getLong("chunkSize"));
        System.out.println(params.getString("type"));
//        jobLauncher.run(jobCreator.make(params), params);
    }

    private JobParameters makeJobParameters(ApplicationArguments args) {
        return converter.getJobParameters(args);
    }
}
