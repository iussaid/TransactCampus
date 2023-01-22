**Functionality**

------------

1. Fetch a list of images from picsum photos API (https://picsum.photos/) and show them in RecyclerView using StaggeredGrid Layout Manager.
2. User can view the list of images using smooth infinite scroll using paging API.
3. When an image is selected from RecyclerView it will load the full-screen image with pinch zoom in/out feature.
4. User can share the image from full-screen view.
5. User can set the image as wallpaper from full-screen view.
6. The image list is cached into local DB, so the list of images are available offline.
7. The app supports sorting(A-Z) (Z-A) of images.
8. The app supports filtering of images by author.
9. The app supports two types of layout Grid (2 Columns) & List.

**Architecture**

------------


The app uses clean architecture with `MVVM(Model View View Model)` design pattern. MVVM provides better separation of concern, easier testing, Live data & lifecycle awareness, etc.

**UI**

------------


The UI consists of two screen

`MainActivity.java` - Initial screen. Shows a list of images.
`ImageActivity.java` - Shows full-screen view of the image with additional options.

**Model**

------------

Model is generated from `JSON` data into a Java data class (`ImagesModel.java`).

**Tech Stack**

------------


1. Android AppCompat, Material Support.
2. Android View Binding
3. [Volley](https://github.com/google/volley "Volley") for REST API communication.
4. Lifecycle, ViewModel
5. [Glide](https://github.com/bumptech/glide "Glide") for image loading.
6. Custom fileprovider for writing & reading files into internal storage.
7. [zoomage ](https://github.com/jsibbold/zoomage "zoomage") for zoom in/out image.

**Future Functionality**

------------


1. Save Images to Gallery.
2. Edit Image.
3. Provide options to utilize the grayscale etc, options from Picsum.
