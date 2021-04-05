package io.easeci.core.workspace.projects;

import io.easeci.core.engine.pipeline.EasefileObjectModel;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

/**
 * An interface that is created for deal with IO operation on pipeline parsed files.
 * It was created in order to make code clear and keep Single Responsible of class.
 * Logic was moved from other class in engine packet.
 * @author Karol Meksuła
 * 2021-04-03
 * */
public interface PipelineIO {

    /**
     * Use it to create a file to serialized EasefileObjectModel store.
     * Logic in this method should creates a file in correct established location ../workspace/projects
     * @return path that indicates where file was created.
     * */
    Path createPipelineFile();

    /**
     * Use it to write EasefileObjectModel to file.
     * Under the hood this method must serialize pipeline in own way.
     * Default concept is to store Pipeline as a bytes encoded by Base64.
     * @param pipelineFile is a file that must exists on storage where application has to write encoded bytes.
     * @param easefileObjectModel is normal POJO object with Pipeline representation.
     * @return path that indicates where file was just saved.
     * */
    Path writePipelineFile(Path pipelineFile, EasefileObjectModel easefileObjectModel);

    /**
     * Use this method to load Pipeline file. This method must implement reversed flow of 'writePipelineFile()',
     * so it should read, deserialize and map content to EasefileObjectModel.
     * @param pipelineId is a id of pipeline from project-structure.json file
     * @return optional value with EasefileObjectModel. If Pipeline not exists optional wrapper will be empty.
     * */
    Optional<EasefileObjectModel> loadPipelineFile(UUID pipelineId);
}
