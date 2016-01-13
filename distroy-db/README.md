# db migrations

see documentation here: http://mybatis.github.io/migrations-maven-plugin/

# Examples

mvn migration:init

mvn migration:status

mvn migration:up

mvn migration:new -Dmigration.description="new table creation"

mvn migration:down -Dmigration.down.steps=1

