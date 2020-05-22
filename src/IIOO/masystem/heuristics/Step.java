package IIOO.masystem.heuristics;

import java.awt.*;

public class Step {
        public Step parent;
        public int row, col;

        public Step(int col, int row, Step parent) {
            this.col = col;
            this.row = row;
            this.parent = parent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null)
                return false;
            if (this.getClass() != o.getClass())
                return false;
            Step other = (Step) o;
            return row == other.row && col == other.col;
        }

        @Override
        public int hashCode() {
            return new Point(this.col, this.row).hashCode();
        }
}
