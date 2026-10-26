/* JAME LMS: Sprint 1 browser prototype. No database or authentication yet. */
const { courses } = window.JAME_DATA;
const U = window.JAME_UTILS;
const app = document.getElementById("app");
const DEMO_DATE = "2026-10-26";

function icon(name){
  const icons = {
    book: '<path d="M4 5h14a2 2 0 0 1 2 2v12H6a2 2 0 0 1-2-2z"/><path d="M4 5a2 2 0 0 0-2 2v10a4 4 0 0 0 4 4h14"/>',
    home: '<path d="m3 10 9-7 9 7v10a1 1 0 0 1-1 1h-5v-7H9v7H4a1 1 0 0 1-1-1z"/>',
    grade: '<path d="m12 2 3.1 6.4 7 .9-5 4.9 1.2 7-6.3-3.4-6.3 3.4 1.2-7-5-4.9 7-.9z"/>',
    list: '<rect x="4" y="3" width="16" height="18" rx="2"/><path d="M8 8h8M8 12h8M8 16h5"/>',
    calendar: '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M7 3v4M17 3v4M3 10h18"/>',
    arrow: '<path d="M5 12h14m-6-6 6 6-6 6"/>',
    back: '<path d="M19 12H5m6 6-6-6 6-6"/>',
    search: '<circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/>',
    mail: '<rect x="2" y="5" width="20" height="14" rx="2"/><path d="m3 6 9 7 9-7"/>',
    check: '<path d="m5 12 4 4L19 6"/>',
    clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
    megaphone: '<path d="m3 11 16-6v14L3 13zM7 14l2 6h3l-2-7"/>',
  };
  return `<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${icons[name] || icons.book}</svg>`;
}

function shell(content, current = "courses"){
  return `
    <div class="site-shell">
      <header class="topbar">
        <a href="#/courses" class="brand" aria-label="JAME LMS courses"><span class="brand-mark">J</span><span>JAME <strong>LMS</strong></span></a>
        <div class="topbar-right"><span class="demo-pill">Student demo</span><span class="avatar" aria-label="Demo student">DS</span></div>
      </header>
      <div class="site-layout">
        <nav class="sidebar" aria-label="Main navigation">
          <p class="nav-caption">WORKSPACE</p>
          <a class="nav-link ${current === "courses" ? "active" : ""}" href="#/courses">${icon("home")} Courses</a>
          <a class="nav-link ${current === "feedback" ? "active" : ""}" href="#/feedback">${icon("grade")} Grades & feedback</a>
          <div class="sidebar-bottom"><span class="indicator-dot"></span> Sprint 1 frontend prototype <span class="small-note">Sample data only</span></div>
        </nav>
        <main class="main" id="main-content">${content}</main>
      </div>
    </div>`;
}

function welcome(){
  return `
    <main class="welcome-screen" id="main-content">
      <div class="welcome-deco deco-a"></div><div class="welcome-deco deco-b"></div>
      <div class="welcome-inner">
        <div class="welcome-kicker">A simpler way to learn</div>
        <div class="welcome-logo">JAME LMS<span class="logo-dot">.</span></div>
        <h1>Everything for class.<br><em>Nothing in your way.</em></h1>
        <p>Find your courses, upcoming assignments, grades, and instructor feedback without digging through menus.</p>
        <a href="#/courses" class="btn btn-dark btn-large">Open student dashboard ${icon("arrow")}</a>
        <div class="welcome-footer">CS3141 SPRINT 1 <span aria-hidden="true">•</span> INTERACTIVE PROTOTYPE</div>
      </div>
    </main>`;
}

function courseCard(course){
  const next = U.upcoming(course, DEMO_DATE);
  const avg = U.average(course);
  const href = `#/course/${course.id}`;
  return `<article class="course-card accent-${course.accent}">
    <div class="card-top"><span class="course-code">${U.escapeHtml(course.code)}</span><span class="course-section">${U.escapeHtml(course.section)}</span></div>
    <a class="course-card-title" href="${href}">${U.escapeHtml(course.title)} ${icon("arrow")}</a>
    <div class="course-card-metrics">
      <a class="metric metric-due" href="#/course/${course.id}/assignments">
        <span class="metric-label">NEXT ASSIGNMENT</span>
        <strong>${next ? U.escapeHtml(next.title) : "All caught up"}</strong>
        <span class="metric-detail">${next ? `Due ${U.dateLabel(next.due)}` : "No upcoming work"}</span>
      </a>
      <a class="metric metric-grade" href="#/course/${course.id}/grades">
        <span class="metric-label">CURRENT GRADE</span>
        <strong>${avg === null ? "N/A" : `${avg}%`}</strong>
        <span class="metric-detail">View grades & feedback ${icon("arrow")}</span>
      </a>
    </div>
    <a class="instructor" href="mailto:${U.escapeHtml(course.email)}">${icon("mail")}
      <span><span class="instructor-label">INSTRUCTOR</span><strong>${U.escapeHtml(course.instructor)}</strong></span>
      <span class="mail-chevron">${icon("arrow")}</span>
    </a>
  </article>`;
}

function dashboard(){
  return shell(`
    <div class="page-heading">
      <div><div class="eyebrow">FALL 2026 / STUDENT PORTAL</div><h1><span class="heading-highlight">Courses</span></h1><p>One place for everything happening in your classes.</p></div>
      <div class="count-pill">${courses.length} active courses</div>
    </div>
    <div class="section-toolbar">
      <div class="section-title"><span class="section-square"></span><h2>My courses</h2></div>
      <label class="search-field">${icon("search")}<span class="sr-only">Search courses</span><input id="course-search" type="search" placeholder="Search courses" autocomplete="off"></label>
    </div>
    <div id="course-results" class="course-grid" aria-live="polite">${courses.map(courseCard).join("")}</div>
    <div class="help-strip">${icon("check")} <span>Dashboard is interactive. Select a course, upcoming assignment, or grade to explore the demo.</span></div>
  `);
}

function breadcrumb(course, final = "Overview"){
  return `<nav class="breadcrumbs" aria-label="Breadcrumb"><a href="#/courses">Courses</a><span>/</span><a href="#/course/${course.id}">${U.escapeHtml(course.code)}</a>${final === "Overview" ? "" : `<span>/</span><span aria-current="page">${U.escapeHtml(final)}</span>`}</nav>`;
}

function courseNav(course, active){
  return `<nav class="tabs" aria-label="${U.escapeHtml(course.code)} sections">
    <a ${active === "overview" ? 'aria-current="page"' : ""} class="tab ${active === "overview" ? "selected" : ""}" href="#/course/${course.id}">Overview</a>
    <a ${active === "assignments" ? 'aria-current="page"' : ""} class="tab ${active === "assignments" ? "selected" : ""}" href="#/course/${course.id}/assignments">Assignments</a>
    <a ${active === "grades" ? 'aria-current="page"' : ""} class="tab ${active === "grades" ? "selected" : ""}" href="#/course/${course.id}/grades">Grades & feedback</a>
    <a ${active === "announcements" ? 'aria-current="page"' : ""} class="tab ${active === "announcements" ? "selected" : ""}" href="#/course/${course.id}/announcements">Announcements</a>
  </nav>`;
}

function courseHero(course){
  return `<div class="course-hero accent-${course.accent}">
    <div><span class="hero-tag">${U.escapeHtml(course.code)} • ${U.escapeHtml(course.section)}</span><h1>${U.escapeHtml(course.title)}</h1><p>${U.escapeHtml(course.description)}</p></div>
    <div class="hero-instructor"><span>INSTRUCTOR</span><strong>${U.escapeHtml(course.instructor)}</strong><a href="mailto:${U.escapeHtml(course.email)}">Contact ${icon("arrow")}</a></div>
  </div>`;
}

function assignmentRow(course, assignment){
  return `<a href="#/course/${course.id}/assignment/${assignment.id}" class="assignment-row">
    <span class="list-icon">${icon("list")}</span>
    <span class="assignment-main"><strong>${U.escapeHtml(assignment.title)}</strong><small>Due ${U.dateLabel(assignment.due)}</small></span>
    <span class="status ${assignment.score !== null ? "status-graded" : assignment.status === "In progress" ? "status-progress" : "status-pending"}">${U.escapeHtml(assignment.status)}</span>
    <span class="assignment-points">${assignment.score === null ? `— / ${assignment.points}` : `${assignment.score} / ${assignment.points}`}</span>
    ${icon("arrow")}
  </a>`;
}

function overview(course){
  const next = U.upcoming(course, DEMO_DATE);
  const graded = course.assignments.find(a => a.score !== null && a.feedback);
  const latestAnnouncement = course.announcements[0];
  return `${breadcrumb(course)}${courseHero(course)}${courseNav(course, "overview")}
    <div class="overview-grid">
      <section class="surface overview-primary">
        <div class="surface-title"><h2>Upcoming assignments</h2><a href="#/course/${course.id}/assignments">See all ${icon("arrow")}</a></div>
        ${next ? assignmentRow(course, next) : '<div class="empty-inline">No upcoming assignments.</div>'}
        <div class="overview-divider"></div>
        <div class="surface-title"><h2>Recent feedback</h2><a href="#/course/${course.id}/grades">View grades ${icon("arrow")}</a></div>
        ${graded ? `<a class="feedback-preview" href="#/course/${course.id}/assignment/${graded.id}">
          <span class="feedback-check">${icon("check")}</span><span><strong>${U.escapeHtml(graded.title)}</strong><small>${U.escapeHtml(graded.feedback)}</small></span>
          <b>${U.percentLabel(graded.score, graded.points)}</b></a>` : '<div class="empty-inline">Instructor feedback will appear here after grading.</div>'}
      </section>
      <aside class="surface announcements-preview">
        <div class="surface-title"><h2>Latest announcement</h2></div>
        ${latestAnnouncement ? `<div class="announcement-icon">${icon("megaphone")}</div><div class="date-small">${U.dateLabel(latestAnnouncement.date)}</div><h3>${U.escapeHtml(latestAnnouncement.title)}</h3><p>${U.escapeHtml(latestAnnouncement.body)}</p>` : '<p>No announcements posted yet.</p>'}
        <a class="text-link" href="#/course/${course.id}/announcements">All announcements ${icon("arrow")}</a>
      </aside>
    </div>`;
}

function assignmentsPage(course){
  return `${breadcrumb(course, "Assignments")}${courseHero(course)}${courseNav(course, "assignments")}
    <section class="surface full-surface">
      <div class="surface-title title-with-search"><div><h2>Assignments</h2><p class="muted">Select an assignment to view its details and feedback.</p></div>
        <label class="search-field">${icon("search")}<span class="sr-only">Search assignments</span><input id="assignment-search" type="search" placeholder="Search assignments"></label>
      </div>
      <div id="assignment-results" class="assignment-list" aria-live="polite">${course.assignments.map(a => assignmentRow(course, a)).join("")}</div>
    </section>`;
}

function gradesPage(course){
  const grade = U.average(course);
  return `${breadcrumb(course, "Grades & feedback")}${courseHero(course)}${courseNav(course, "grades")}
    <div class="grade-summary"><span>GRADED WORK AVERAGE</span><strong>${grade === null ? "N/A" : `${grade}%`}</strong><small>Based on graded assignments in this demo only</small></div>
    <section class="surface full-surface"><div class="surface-title"><h2>Scores & instructor comments</h2></div>
      ${course.assignments.map(a => `<div class="grade-entry">
        <div><a href="#/course/${course.id}/assignment/${a.id}"><strong>${U.escapeHtml(a.title)}</strong></a><small>${a.score === null ? "Not graded yet" : "Graded assignment"}</small>
          <p class="grade-comment">${a.feedback ? `“${U.escapeHtml(a.feedback)}”` : "No feedback available yet."}</p></div>
        <div class="grade-score">${a.score === null ? "—" : U.percentLabel(a.score, a.points)}<small>${a.score === null ? `— / ${a.points}` : `${a.score} / ${a.points}`}</small></div>
      </div>`).join("")}
    </section>`;
}

function announcementsPage(course){
  return `${breadcrumb(course, "Announcements")}${courseHero(course)}${courseNav(course, "announcements")}
    <section class="surface full-surface"><div class="surface-title"><h2>Announcements</h2></div>
      ${course.announcements.map(a => `<article class="announcement-row"><span class="announcement-icon small">${icon("megaphone")}</span><div><time>${U.dateLabel(a.date)}</time><h3>${U.escapeHtml(a.title)}</h3><p>${U.escapeHtml(a.body)}</p></div></article>`).join("")}
    </section>`;
}

function assignmentPage(course, assignment){
  const hasScore = assignment.score !== null;
  return `${breadcrumb(course, "Assignment")}
    <div class="detail-back"><a href="#/course/${course.id}/assignments">${icon("back")} Back to assignments</a></div>
    <div class="assignment-detail-header"><span class="detail-type">${U.escapeHtml(course.code)} / ASSIGNMENT</span><h1>${U.escapeHtml(assignment.title)}</h1><p>Due ${U.dateLabel(assignment.due)}</p></div>
    <div class="detail-grid"><section class="surface detail-main"><h2>Assignment status</h2>
      <div class="detail-field"><span>Status</span><strong>${U.escapeHtml(assignment.status)}</strong></div>
      <div class="detail-field"><span>Possible points</span><strong>${assignment.points}</strong></div>
      <div class="detail-field"><span>Score</span><strong>${hasScore ? `${assignment.score} / ${assignment.points} (${U.percentLabel(assignment.score, assignment.points)})` : "Not graded"}</strong></div>
      <h2 class="feedback-heading">Instructor feedback</h2><p class="detail-feedback">${assignment.feedback ? U.escapeHtml(assignment.feedback) : "No instructor feedback has been posted for this assignment yet."}</p>
    </section><aside class="surface detail-aside"><h2>Quick navigation</h2><p>See the full list of assignments or review other grades for this course.</p>
      <a class="side-action" href="#/course/${course.id}/assignments">All assignments ${icon("arrow")}</a><a class="side-action" href="#/course/${course.id}/grades">Grades & feedback ${icon("arrow")}</a></aside></div>`;
}

function feedbackHub(){
  const graded = courses.flatMap(c => c.assignments.filter(a => a.score !== null).map(a => ({ course: c, assignment: a })));
  return shell(`<div class="page-heading"><div><div class="eyebrow">ALL COURSES / ONE PLACE</div><h1><span class="heading-highlight">Grades & feedback</span></h1><p>Instructor comments from all your demo courses, together.</p></div></div>
    <div class="hub-grid">${graded.map(({course, assignment}) => `<a class="hub-card" href="#/course/${course.id}/assignment/${assignment.id}"><span class="hub-code">${U.escapeHtml(course.code)}</span><strong>${U.escapeHtml(assignment.title)}</strong><span class="hub-course">${U.escapeHtml(course.title)}</span><span class="hub-feedback">${U.escapeHtml(assignment.feedback || "No feedback yet.")}</span><span class="hub-footer"><b>${U.percentLabel(assignment.score, assignment.points)}</b> Review details ${icon("arrow")}</span></a>`).join("")}</div>
    <p class="demo-disclaimer">These scores and comments are example content created for a software prototype, not school records.</p>
  `, "feedback");
}

function renderRoute(){
  const route = U.parseRoute(window.location.hash);
  if(route.page === "welcome"){
    app.innerHTML = welcome();
  }else if(route.page === "courses"){
    app.innerHTML = dashboard();
  }else if(route.page === "feedback"){
    app.innerHTML = feedbackHub();
  }else{
    const course = courses.find(c => c.id === route.courseId);
    if(!course){ app.innerHTML = shell('<section class="surface full-surface"><h1>Course not found</h1><a href="#/courses">Return to courses</a></section>'); return; }
    let html = "";
    if(route.page === "course"){ html = overview(course); }
    if(route.page === "assignments"){ html = assignmentsPage(course); }
    if(route.page === "grades"){ html = gradesPage(course); }
    if(route.page === "announcements"){ html = announcementsPage(course); }
    if(route.page === "assignment"){
      const a = course.assignments.find(item => item.id === route.assignmentId);
      html = a ? assignmentPage(course, a) : '<section class="surface"><h1>Assignment not found</h1></section>';
    }
    app.innerHTML = shell(html);
  }
  document.title = route.page === "welcome" ? "JAME LMS | Welcome" : `JAME LMS | ${route.page[0].toUpperCase() + route.page.slice(1)}`;
  window.scrollTo(0, 0);
}

window.addEventListener("hashchange", renderRoute);
document.addEventListener("input", event => {
  if(event.target.id === "course-search"){
    const filtered = U.filterCourses(courses, event.target.value);
    const container = document.getElementById("course-results");
    if(container){ container.innerHTML = filtered.length ? filtered.map(courseCard).join("") : '<div class="empty-state">No courses match your search. Try a course name or code.</div>'; }
  }
  if(event.target.id === "assignment-search"){
    const route = U.parseRoute(window.location.hash);
    const course = courses.find(c => c.id === route.courseId);
    const container = document.getElementById("assignment-results");
    if(course && container){
      const filtered = U.filterAssignments(course.assignments, event.target.value);
      container.innerHTML = filtered.length ? filtered.map(a => assignmentRow(course, a)).join("") : '<div class="empty-state">No assignments match your search.</div>';
    }
  }
});
renderRoute();
