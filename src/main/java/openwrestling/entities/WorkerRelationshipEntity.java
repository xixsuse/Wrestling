package openwrestling.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@DatabaseTable(tableName = "worker_relationships")
public class WorkerRelationshipEntity extends Entity {

    @DatabaseField(generatedId = true)
    private long relationshipID;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private WorkerEntity worker1;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private WorkerEntity worker2;

    @DatabaseField
    private int level;
}
