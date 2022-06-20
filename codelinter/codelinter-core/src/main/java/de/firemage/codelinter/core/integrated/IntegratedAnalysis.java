package de.firemage.codelinter.core.integrated;

import de.firemage.codelinter.core.Problem;
import de.firemage.codelinter.core.dynamic.DockerConsoleRunner;
import de.firemage.codelinter.core.dynamic.DockerRunnerException;
import de.firemage.codelinter.core.dynamic.DynamicAnalysis;
import de.firemage.codelinter.core.dynamic.TestRunResult;
import de.firemage.codelinter.core.file.UploadedFile;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IntegratedAnalysis implements AutoCloseable {
    private final UploadedFile file;
    private final Path jar;
    private final Path tmpPath;
    private final StaticAnalysis staticAnalysis;
    private DynamicAnalysis dynamicAnalysis;

    public IntegratedAnalysis(UploadedFile file, Path jar, Path tmpPath, Consumer<String> statusConsumer)
        throws ModelBuildException, IOException {
        this.file = file;
        this.jar = jar;
        this.tmpPath = tmpPath;

        this.staticAnalysis = new StaticAnalysis(file, jar, statusConsumer);
        this.dynamicAnalysis = new DynamicAnalysis(List.of());
    }

    public void runDynamicAnalysis(Path tests, Consumer<String> statusConsumer)
        throws IOException, InterruptedException, DockerRunnerException, URISyntaxException {
        DockerConsoleRunner runner = new DockerConsoleRunner(
            Path.of(this.getClass().getResource("/executor.jar").toURI()),
            Path.of(this.getClass().getResource("/agent.jar").toURI()),
            tests,
            this.tmpPath);
        List<TestRunResult> results = runner.runTests(this.staticAnalysis, this.jar, statusConsumer);
        this.dynamicAnalysis = new DynamicAnalysis(results);
    }

    public List<Problem> lint(List<IntegratedCheck> checks) {
        //MethodAnalysis methodAnalysis = new MethodAnalysis(this.model);
        //methodAnalysis.run();

        List<Problem> problems = new ArrayList<>();
        for (IntegratedCheck check : checks) {
            problems.addAll(check.run(this.staticAnalysis, this.dynamicAnalysis, this.file.getFile()));
        }

        return problems;
    }

    public StaticAnalysis getStaticAnalysis() {
        return staticAnalysis;
    }

    public DynamicAnalysis getDynamicAnalysis() {
        return dynamicAnalysis;
    }

    @Override
    public void close() throws IOException {
        this.staticAnalysis.close();
    }
}
