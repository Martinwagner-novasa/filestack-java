package com.filestack.transforms;

import com.filestack.FileLink;
import com.filestack.FilestackClient;
import com.filestack.responses.StoreResponse;
import com.filestack.transforms.tasks.StoreOptions;
import com.google.gson.JsonObject;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * {@link Transform Transform} subclass for image transformations.
 */
public class ImageTransform extends Transform {

  public ImageTransform(FilestackClient fsClient, String source) {
    super(fsClient, source);
  }

  public ImageTransform(FileLink fileLink) {
    super(fileLink);
  }

  /**
   * Debugs the transformation as built so far, returning explanations of any issues.
   *
   * @return {@link JsonObject JSON} report for transformation
   * @throws IOException for network failures, invalid handles, or invalid security
   */
  public JsonObject debug() throws IOException {
    String tasksString = getTasksString();

    if (apiKey != null) {
      return fsService.transformDebugExt(apiKey, tasksString, source).execute().body();
    } else {
      return fsService.transformDebug(tasksString, source).execute().body();
    }
  }

  public FileLink store() throws IOException {
    return store(null);
  }

  /**
   * Stores the result of a transformation into a new file.
   * https://www.filestack.com/docs/image-transformations/store
   *
   * @param storeOptions configure where and how your file is stored
   * @return new {@link FileLink FileLink} pointing to the file
   * @throws IOException for network failures, invalid handles, or invalid security
   */
  public FileLink store(StoreOptions storeOptions) throws IOException {
    if (storeOptions == null) {
      storeOptions = new StoreOptions();
    }
    addTask(storeOptions);

    StoreResponse response;
    String tasksString = getTasksString();

    if (apiKey != null) {
      response = fsService.transformStoreExt(apiKey, tasksString, source).execute().body();
    } else {
      response = fsService.transformStore(tasksString, source).execute().body();
    }

    String handle = response.getUrl().split("/")[3];
    return new FileLink(apiKey, handle, security);
  }

  /**
   * Add a new transformation to the chain.
   * Tasks are executed in the order they are added.
   *
   * @param task any one of the available {@link ImageTransformTask ImageTransformTask} subclasses
   */
  public ImageTransform addTask(ImageTransformTask task) {
    if (task == null) {
      throw new IllegalArgumentException("Cannot add null task to image transform");
    }
    tasks.add(task);
    return this;
  }

  // Async method wrappers

  /**
   * Async, observable version of {@link #debug()}.
   * Throws same exceptions.
   */
  public Single<JsonObject> debugAsync() {
    return Single.fromCallable(new Callable<JsonObject>() {
      @Override
      public JsonObject call() throws Exception {
        return debug();
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.single());
  }

  /**
   * Async, observable version of {@link #store()}.
   * Throws same exceptions.
   */
  public Single<FileLink> storeAsync() {
    return storeAsync(null);
  }

  /**
   * Async, observable version of {@link #store(StoreOptions)}.
   * Throws same exceptions.
   */
  public Single<FileLink> storeAsync(final StoreOptions storeOptions) {
    return Single.fromCallable(new Callable<FileLink>() {
      @Override
      public FileLink call() throws Exception {
        return store(storeOptions);
      }
    })
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.single());
  }
}
