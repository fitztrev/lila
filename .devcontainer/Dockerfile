FROM ubuntu:focal-20220415

SHELL ["/bin/bash", "-c"]

RUN apt-get update \
    && apt update \
    && apt-get install -y build-essential ca-certificates curl gnupg locales sudo wget

ENV TZ=Etc/GMT
RUN sudo ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && sudo echo $TZ > /etc/timezone
RUN locale-gen "en_US.UTF-8"

# Install coursier (dependency of bloop)
RUN curl -fL https://github.com/coursier/launchers/raw/master/cs-x86_64-pc-linux.gz | gzip -d > cs \
    && chmod +x cs \
    && sudo mv cs /usr/local/bin/cs \
    && cs setup --yes

# Add mongodb apt source
RUN wget -qO - https://www.mongodb.org/static/pgp/server-5.0.asc | sudo apt-key add -
RUN echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/5.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb-org-5.0.list

RUN sudo apt-get update && sudo apt update \
  && sudo apt-get install -y git-all golang-go mongodb-org nginx parallel psmisc python3.9 python3-pip redis-server unzip vim zip

# Cleanup
RUN sudo apt-get autoremove -y \
  && sudo apt-get clean

# Add nginx site config
COPY build/nginx/lichess.conf /etc/nginx/sites-enabled/
RUN rm /etc/nginx/sites-enabled/default
