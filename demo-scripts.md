# build image
docker build -t us-central1-docker.pkg.dev/persian-java-talk/gemini-spring-ai/cloudland-detect-scene-changes .
# Authenticate to GAR
gcloud auth configure-docker us-central1-docker.pkg.dev
# Push Image to GAR
docker push us-central1-docker.pkg.dev/persian-java-talk/gemini-spring-ai/cloudland-detect-scene-changes:latest
# Deploy on Cloud Run Jobs
gcloud run jobs deploy cloudland-detect-scene-change --image us-central1-docker.pkg.dev/persian-java-talk/gemini-spring-ai/cloudland-detect-scene-changes --region us-central1 --execute-now