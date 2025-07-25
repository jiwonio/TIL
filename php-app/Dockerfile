# Apache 웹 서버가 포함된 공식 PHP 이미지를 사용합니다.
FROM php:8.2-apache

# 1. 시스템 의존성 및 빌드 도구 설치
RUN apt-get update && apt-get install -y \
    build-essential \
    libaio1 \
    libzip-dev \
    unzip \
    wget \
    && rm -rf /var/lib/apt/lists/*

# 2. Oracle Instant Client 다운로드 및 설치
ARG ORACLE_CLIENT_URL_BASIC="https://download.oracle.com/otn_software/linux/instantclient/2113000/instantclient-basic-linux.x64-21.13.0.0.0dbru.zip"
ARG ORACLE_CLIENT_URL_SDK="https://download.oracle.com/otn_software/linux/instantclient/2113000/instantclient-sdk-linux.x64-21.13.0.0.0dbru.zip"
ENV ORACLE_HOME=/opt/oracle/instantclient
# LD_LIBRARY_PATH 초기화 및 설정
ENV LD_LIBRARY_PATH=/opt/oracle/instantclient

RUN mkdir -p $ORACLE_HOME \
    && cd /tmp \
    && wget --header="Cookie: oraclelicense=accept-securebackup-cookie" -O basic.zip ${ORACLE_CLIENT_URL_BASIC} \
    && wget --header="Cookie: oraclelicense=accept-securebackup-cookie" -O sdk.zip ${ORACLE_CLIENT_URL_SDK} \
    && unzip basic.zip -d $ORACLE_HOME \
    && unzip sdk.zip -d $ORACLE_HOME \
    && mv $ORACLE_HOME/instantclient_21_13/* $ORACLE_HOME \
    && rm -rf $ORACLE_HOME/instantclient_21_13 \
    && rm -f basic.zip sdk.zip \
    && echo $ORACLE_HOME > /etc/ld.so.conf.d/oracle-instantclient.conf \
    && ldconfig

# 3. PHP 확장 설치
RUN docker-php-ext-install pdo_mysql \
    && echo "instantclient,$ORACLE_HOME" | pecl install oci8-3.2.1 \
    && docker-php-ext-enable oci8 \
    && docker-php-ext-configure pdo_oci --with-pdo-oci=instantclient,$ORACLE_HOME \
    && docker-php-ext-install pdo_oci

# 4. 애플리케이션 코드 복사
COPY . /var/www/html/

# 5. 포트 노출
EXPOSE 80