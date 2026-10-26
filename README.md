# JAME LMS

CS3141 Team 18, Sprint 1 frontend prototype. The design is based on the team's orange **JAME LMS** loading screen and Courses dashboard mockups.

## Run it

**Simplest:** download the files together and double-click `index.html` to open the prototype in Chrome or Edge. No installation or database is needed.

**Optional local server:** from the project folder run `python -m http.server 8000`, then visit `http://localhost:8000`.

## Demo walkthrough

1. Select **Open student dashboard** on the JAME LMS start screen.
2. On the Courses page, search for `CS3141`, then open **Software Engineering**.
3. Select the **Assignments** tab. Select an assignment to see its status and points.
4. Open **Grades & feedback** and select a graded assignment to read instructor comments.
5. Use the sidebar **Grades & feedback** link to see comments across all courses.
6. Click the instructor contact link to see the mailto action (the addresses are placeholders).

## Sprint 1 implementation

- Splash / start page inspired by the team's loading-screen mockup
- Responsive Courses dashboard with course cards, upcoming assignments, displayed grades, and instructor contact links
- Course home pages with overview, assignments, grades, and announcements
- Click-through assignment details with grade and comment display
- Course search and assignment search
- In-browser navigation (hash URLs, so links work without a router or backend)
- Automated tests for grade averages, due assignments, searches, route parsing, and HTML escaping

**Important limitations:** All courses, dates, grades, instructors and comments are **fictional sample data** stored in `data.js`. They are not real university records. There is no login, database, assignment upload, or instructor editing in this sprint. The displayed course average calculates from scored demo assignments only; it does not use Canvas weighting rules. Dates are fixed to October 26, 2026 for a consistent Sprint 1 demonstration. The frontend is not production-ready.

## Files

- `index.html`: Browser entry point
- `styles.css`: Page layout and responsive styling
- `data.js`: Fictional test courses, assignments and announcements
- `utils.js`: Reusable helpers for search, route parsing, due dates and grade averages
- `app.js`: Interactive pages and links
- `tests/lms.test.js`: Automated tests

## Tests

Requires Node.js 18 or newer:

```sh
node --test tests/*.test.js
```

## Suggested Chapter 3 / Chapter 4 documentation

**Implementation:** Explain that the frontend uses HTML, CSS, and JavaScript without third-party runtime libraries. Document the welcome view, dashboard, course overview, assignments, and grades/feedback screens. Include *screenshots of the running browser application*, not source code.

**Validation:** Use `node --test tests/*.test.js` and record the real results. Manually verify: start -> dashboard; course card -> course overview; upcoming assignment -> assignments; current grade -> grades; grade entry -> feedback detail; searches -> filtered lists; and mobile layout -> readable cards. Only report tests actually performed.

**Next sprint:** Connect a backend, implement role-based login, replace demo data with authenticated course records, and add submission/grading workflows once the team approves a plan.
