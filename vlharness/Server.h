/* 
 * File:   Server.h
 * Author: reggie
 *
 * Created on April 26, 2011, 3:19 PM
 */

#ifndef SERVER_H
#define	SERVER_H

class Server {
private:

    std::string name;
    std::string protocol;
    std::string url;
    std::string params;
    int metric;

public:
    Server(const std::string& name, const std::string& protocol, const std::string& url, const std::string& params, int metric);
    Server(const Server& orig);
    virtual ~Server();
    virtual std::string getName();
    virtual std::string getProtocol();
    virtual std::string getUrl();
    virtual std::string getParams();
    virtual int getMetric();
    virtual void setMetric(int metric);
    virtual int getDistance();

private:

};

#endif	/* SERVER_H */

