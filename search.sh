curl --header "content-type: application/JSON" 'localhost:9200/questions2_cases-000001/_search' -d '{
 "query": { "match_all": {} }, "size": 10 i
}'
