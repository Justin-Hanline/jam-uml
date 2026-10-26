/* Pure helpers shared between the browser application and automated tests. */
const JAME_UTILS = {
  average(course){
    const graded = course.assignments.filter(a => Number.isFinite(a.score) && a.points > 0);
    if(!graded.length){
      return null;
    }
    const earned = graded.reduce((total, assignment) => total + assignment.score, 0);
    const possible = graded.reduce((total, assignment) => total + assignment.points, 0);
    return Math.round((earned / possible) * 100);
  },

  upcoming(course, today = "2026-10-26"){
    const unfinished = course.assignments.filter(a => a.score === null && a.due >= today);
    return [...unfinished].sort((a, b) => a.due.localeCompare(b.due))[0] || null;
  },

  filterCourses(courses, query){
    const search = query.trim().toLowerCase();
    if(!search){ return courses; }
    return courses.filter(c => `${c.title} ${c.code} ${c.instructor}`.toLowerCase().includes(search));
  },

  filterAssignments(assignments, query){
    const search = query.trim().toLowerCase();
    if(!search){ return assignments; }
    return assignments.filter(a => `${a.title} ${a.status}`.toLowerCase().includes(search));
  },

  parseRoute(hash){
    const path = (hash || "#/welcome").replace(/^#\/?/, "");
    const parts = path.split("/").filter(Boolean);
    if(parts[0] === "courses" && parts.length === 1){ return { page: "courses" }; }
    if(parts[0] === "feedback" && parts.length === 1){ return { page: "feedback" }; }
    if(parts[0] === "course" && parts[1]){
      if(parts.length === 2){ return { page: "course", courseId: parts[1] }; }
      if(["assignments", "grades", "announcements"].includes(parts[2]) && parts.length === 3){
        return { page: parts[2], courseId: parts[1] };
      }
      if(parts[2] === "assignment" && parts[3] && parts.length === 4){
        return { page: "assignment", courseId: parts[1], assignmentId: parts[3] };
      }
    }
    return { page: "welcome" };
  },

  dateLabel(date){
    return new Date(`${date}T12:00:00`).toLocaleDateString("en-US", { month: "short", day: "numeric", year: "numeric" });
  },

  percentLabel(score, possible){
    return `${Math.round(score / possible * 100)}%`;
  },

  escapeHtml(value){
    return String(value).replace(/[&<>"']/g, character => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[character]);
  }
};

if(typeof window !== "undefined"){
  window.JAME_UTILS = JAME_UTILS;
}
if(typeof module !== "undefined"){
  module.exports = JAME_UTILS;
}
