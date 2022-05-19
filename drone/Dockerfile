FROM dvicente/python3-lxml:dc8e885932e403519b14f6db1adb93c3deca89ee

WORKDIR /usr/app

RUN apt-get update
RUN apt-get -y install make gstreamer-1.0 gcc libc-dev

RUN pip list

COPY . .

RUN make install

CMD ["make", "run"]