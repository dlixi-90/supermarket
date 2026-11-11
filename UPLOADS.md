# Product images

The product form accepts JPG and PNG files up to 5 MB (maximum 20 million pixels), previews the selected image, and stores a generated image URL in the existing `imageUrl` field. Saving without selecting a file keeps the current image on edits. Existing image URLs continue to work.

Images are stored outside the deployed WAR, in `${catalina.base}/msm-uploads/products` by default. Set the JVM property `msm.upload.dir` or the environment variable `MSM_UPLOAD_DIR` to use another persistent directory. The Tomcat process needs read/write access to this directory. Back it up alongside the database; redeploying the application does not remove these files.

The application serves stored images at `/msm/uploads/products/<generated-name>`. It validates the image content and saves a decoded JPG/PNG under a generated filename; it does not use the uploaded filename as a storage path. Old images are retained when a product image is replaced.

Category images use the same upload validation, persistent storage and image URL endpoint as product images. Category documents store the URL in `imageUrl`; older categories without this field display a placeholder. Updating a category without selecting a new file preserves its existing image.
