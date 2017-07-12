
#### Consumer creation

```{bash}
curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" \
      --data '{"name": "my_consumer_instance", "format": "json", "auto.offset.reset": "earliest"}' \
      http://localhost:8082/consumers/my_json_consumer

```

```
{
    "instance_id": "my_consumer_instance", 
    "base_uri":"http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance"
}
```

#### Topic subscription

```{bash}
curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" --data '{"topics":["TWEETS-PRED"]}' \
 http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance/subscription
```