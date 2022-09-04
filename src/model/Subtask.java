package model;

import utils.Status;

public class Subtask extends Task {
    private Epic epic;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }

    public Task getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
        this.epic.updateStatus();
    }

    @Override
    public void setStatus(Status status) {
        super.setStatus(status);
        epic.updateStatus();
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", epic=" + this.epic.getId() +
                '}';
    }
}
