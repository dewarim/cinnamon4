-- find a folder and all its ancestors:
-- including example for path & depth (level)
with recursive fp (id, parent_id, name, path, ids, level)
as (
  select
    id,
    parent_id,
    name,
    cast(id as varchar(10000)) as path,
    array [id],
    0                          as level
  from folders f
  where f.parent_id is null
  union all
  select
    child.id,
    child.parent_id,
    child.name,
    cast(fp.path || ' > ' || child.id as varchar(10000)) as path,
    array_append(ids, child.id),
    fp.level + 1
  from folders child
    join fp on child.parent_id = fp.id
  where child.parent_id is not null
)
select
  id,
  parent_id,
  name,
  path,
  ids,
  level
from fp
where
  array_length(ids, 1) <= (select array_length(ids, 1)
                           from fp
                           where id = 6)
  and ids <@ (select ids
              from fp
              where id = 6);
