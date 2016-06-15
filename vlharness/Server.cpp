/* 
 * File:   Server.cpp
 * Author: reggie
 * 
 * Created on April 26, 2011, 3:19 PM
 */

#include <string>

#include "Server.h"

Server::Server(const std::string& name, const std::string& protocol, const std::string& url, const std::string& params, int metric) {
    this->name = name;
    this->protocol = protocol;
    this->url = url;
    this->params = params;
    this->metric = metric;
}

std::string Server::getName(){
    return this->name;
}

int Server::getMetric(){
    return this->metric;
}

void Server::setMetric(int metric){
    this->metric = metric;
}

std::string Server::getProtocol(){
    return this->protocol;
}

std::string Server::getUrl(){
    return this->url;
}

std::string Server::getParams(){
    return this->params;
}

//TODO
int Server::getDistance(){
    return 0;
}

Server::Server(const Server& orig) {
}

Server::~Server() {
}

