create table user_subscriptions (
channel_id int NOT null references usr,
subscriber_id int NOT null,
primary key (channel_id, subscriber_id)
)