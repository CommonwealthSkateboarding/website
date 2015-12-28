FROM ingensi/play-framework:latest
MAINTAINER <colin.delargy@gmail.com>

ADD . /app
RUN yum update -y && yum install -y unzip git sass nodej