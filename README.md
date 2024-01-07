# QDynDnsUpdater

A DynDns Updater implemented with Java and Quarkus. Easy to set up with Docker. It supports multiple DynDns accounts and
is able to perform a self check over internet.

## Installation

```.yaml
version: '3.8'
services:
  dyndns:
    image: 'andycandyde/q-dyndns-updater'
    restart: unless-stopped
    environment:
      - UPDATE_URL=https://dyndns.provider.net/nic/update
      - USERNAME=username
      - PASSWORD=password
      - DOMAINS=your.domain.net
      - IP_RESOLVER_TYPE=http_get
      - IP_RESOLVER_HTTP_GET_URL=https://myexternalip.com/raw
```

## Configuration

You can configure the updater via environment variables or json file.

 Config                      | Description                                                                                      | Default Value |           Optional            | Environment variable        | Json                              
-----------------------------|--------------------------------------------------------------------------------------------------|--------------:|:-----------------------------:|-----------------------------|-----------------------------------
 log_memory_info             | Logs memory info before execution.                                                               |         false |               X               | LOG_MEMORY_INFO             | logMemoryInfo                     
 interval                    | Interval of the update execution. (env=5M, json = PT5M)                                          |            5M |               X               | INTERVAL                    | interval                          
 times                       | Wanted execution times. A Value < 1 means endless.                                               |            -1 |               X               | TIMES                       | times                             
 self_check                  | To enable the self check feature.                                                                |         false |               X               | SELF_CHECK                  | selfCheck                         
 self_check_url              | Public URL of the QDynDnsUpdater to execute the self check                                       |               |    Depends on {self_check}    | SELF_CHECK_URL              | selfCheckUrl                      
 update_url                  | URL to send the DynDns update request. (e.g. dynupdate.no-ip.com/nic/update)                     |               |                               | UPDATE_URL                  | dynDnsConfigs.updateUrl           
 username                    | Username to authorize the DynDns update.                                                         |               |                               | USERNAME                    | dynDnsConfigs.username            
 password                    | Password to authorize the DynDns update.                                                         |               |                               | PASSWORD                    | dynDnsConfigs.password            
 domains                     | DynDns domains you have on the DynDns accout. (e.g. domain.net,sub.domain.net)                   |               |                               | DOMAINS                     | dynDnsConfigs.domains             
 user_agent                  | Can be used to set a user header to the DynDns update http request                               |               |               X               | USER_AGENT                  | dynDnsConfigs.userAgent           
 domains_single_update       | With this property set to 'true' each domain will be a single DynDns update http request.        |         false |               X               | DOMAINS_SINGLE_UPDATE       | dynDnsConfigs.domainsSingleUpdate 
 ip_resolver.type            | Defines how the public ip is detected. Possible values are 'static', 'http_get' and 'command'.   |               |                               | IP_RESOLVER_TYPE            | ipResolverType                    
 ip_resolver.http_get.url    | Http get endpoint to receive the public ip as a text. (e.g. https://myexternalip.com/raw)        |               | Depends on {ip_resolver.type} | IP_RESOLVER_HTTP_GET_URL    | ipResolverHttpGetUrl              
 ip_resolver.static.ip       | Static ip address which will be send to the DynDns provider.                                     |               | Depends on {ip_resolver.type} | IP_RESOLVER_STATIC_IP       | ipResolverStaticIp                
 ip_resolver.command.command | A command which can be executed to get the public ip. (e.g. 'wget https://myexternalip.com/raw') |               | Depends on {ip_resolver.type} | IP_RESOLVER_COMMAND_COMMAND | ipResolverCommandCommand          
 ip_resolver.command.timeout | Execution timeout for the command {ip_resolver.command.command}. (env=5M, json = PT5M)           |            5M |               X               | IP_RESOLVER_COMMAND_TIMEOUT | ipResolverCommandTimeout          

### Configuration via environment variables

Just define the environment variables for your docker container.

```yaml
    environment:
      - UPDATE_URL=https://dyndns.provider.net/nic/update
      - USERNAME=username
      - PASSWORD=password
      - DOMAINS=your.domain.net
      - IP_RESOLVER_TYPE=http_get
      - IP_RESOLVER_HTTP_GET_URL=https://myexternalip.com/raw
```

### Configuration via json

Define the path to your config json via environment variables and mount the file into the docker container. With this
configuration you can define multiple DynDns accounts.

```.yaml
version: '3.8'
services:
  dyndns:
    image: 'andycandyde/q-dyndns-updater'
    restart: unless-stopped
    environment:
      - CONFIG_JSON=/home/jboss/config.json
    volumes:
      - ./config.json:/home/jboss/config.json:ro
```

```json
{
  "interval": "PT2M",
  "ipResolverType": "http_get",
  "ipResolverStaticIp": "https://myexternalip.com/raw",
  "dynDnsConfigs": [
    {
      "updateUrl": "https://dyndns.provider.net/nic/update",
      "username": "username",
      "password": "password",
      "domains": [
        "main.net",
        "sub.main.net"
      ]
    },
    {
      "updateUrl": "https://super.provider.it/nic/update",
      "username": "username",
      "password": "password",
      "domains": [
        "more.net",
        "more.more.net"
      ]
    }
  ]
}
```

### Self check

If the self check feature is enabled the service tries to request itself with the url defined in {self_check_url}. So be
sure that your service is available **over internet** with this url. If the self check fails the service tries to get
the public ip and performs a DynDns update if needed. You can give the service a subdomain with a reverse proxy and
update this domain as well.

```.yaml
version: '3.8'
services:
  dyndns:
    image: 'andycandyde/q-dyndns-updater'
    restart: unless-stopped
    environment:
      - SELF_CHECK=true
      - SELF_CHECK_URL=http://test.domain.net
      - UPDATE_URL=https://dyndns.provider.net/nic/update
      - USERNAME=username
      - PASSWORD=password
      - DOMAINS=domain.net,test.domain.net
      - IP_RESOLVER_TYPE=http_get
      - IP_RESOLVER_HTTP_GET_URL=https://myexternalip.com/raw
```

## Links

* Github - https://github.com/andycandy-de/QDynDnsUpdater
* Docker Hub - https://hub.docker.com/r/andycandyde/q-dyndns-updater

## License

MIT License

Copyright (c) 2024 andycandy-de

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.