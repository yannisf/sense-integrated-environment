#!/usr/bin/env python3

import sys
from argparse import ArgumentParser
from configparser import ConfigParser
from time import sleep,time
from kafka import KafkaProducer, errors
from Adafruit_DHT import DHT11, DHT22, read_retry

def parse_args():
    parser = ArgumentParser()
    parser.add_argument('-f', '--file', help='configuration file', default='env.ini')
    parser.add_argument('-s', '--section', help='section to read from configuration file', required=True)
    return parser.parse_args()

def load_conf(file, section):
    config = ConfigParser()
    config.read(file)
    return config[section]

def init_producer(broker):
    while (True):
        try:
            return KafkaProducer(bootstrap_servers=[broker])
        except errors.NoBrokersAvailable:
            print (f'Broker [{broker}] is not available. Retrying in 10 seconds...', file=sys.stderr)
            sys.stderr.flush()
            sleep(10)

def get_sensor(sensor):
    if ('dht11' == sensor.lower()):
        return DHT11
    elif ('dht22' == sensor.lower()):
        return DHT22
    else:
        raise Exception(f'Unknown sensor model [{sensor}]')

def get_line(room, sensor, sensor_type, sensor_serial):
    timestamp = int(time() * 1000)
    humidity, temperature = read_retry(sensor, conf['data_pin'])
    return  f"{timestamp},{room},{sensor_type},{sensor_serial},{humidity},{temperature}"


args = parse_args()
conf = load_conf(args.file, args.section)
sensor = get_sensor(conf['sensor'])
producer = init_producer(conf['kafka'])

while True:
    line = get_line(args.section, sensor, conf['sensor'], conf['serial'])
    producer.send(conf['topic'], line.encode())
    print(line, file=sys.stdout)
    sys.stdout.flush()
    sleep(int(conf['poll_seconds']))


