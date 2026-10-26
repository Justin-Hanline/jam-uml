/* Fictional course, assignment and grade data for the Sprint 1 demo.
   No records are fetched from a school or stored on a server. */
const JAME_DATA = {
  courses: [
    {
      id: "cs3141", code: "CS3141", title: "Software Engineering", section: "R01",
      instructor: "Instructor A", email: "instructor-a@example.edu", accent: "orange",
      description: "Build a team software project through planning, design, implementation, and testing.",
      announcements: [
        { id: "a1", date: "2026-10-23", title: "Sprint 1 demonstration", body: "Bring a working increment and explain the features your group completed." },
        { id: "a2", date: "2026-10-20", title: "Documentation reminder", body: "Update your analysis, implementation, and validation sections during the sprint." }
      ],
      assignments: [
        { id: "sprint1", title: "Sprint 1 Project", due: "2026-10-26", points: 40, score: null, status: "In progress", feedback: null },
        { id: "peer1", title: "Self-Report and Peer Review", due: "2026-10-26", points: 40, score: null, status: "Not submitted", feedback: null },
        { id: "proposal", title: "Project Proposal", due: "2026-10-09", points: 40, score: 36, status: "Graded", feedback: "Clear project goals. Give more detail about the implementation plan." }
      ]
    },
    {
      id: "cs3411", code: "CS3411", title: "Systems Programming", section: "R01",
      instructor: "Instructor B", email: "instructor-b@example.edu", accent: "teal",
      description: "Explore processes, files, memory, and programming close to the operating system.",
      announcements: [
        { id: "a1", date: "2026-10-21", title: "Program submission", body: "Include your build instructions and a short explanation of the tests you ran." }
      ],
      assignments: [
        { id: "prog3", title: "Program 3: Process Utilities", due: "2026-10-30", points: 100, score: null, status: "Not submitted", feedback: null },
        { id: "prog2", title: "Program 2: File Archiver", due: "2026-10-12", points: 100, score: 90, status: "Graded", feedback: "Good implementation. Add more edge-case tests for invalid inputs." },
        { id: "prog1", title: "Program 1: Bit Packing", due: "2026-09-19", points: 100, score: 95, status: "Graded", feedback: "Well organized and easy to run." }
      ]
    },
    {
      id: "ee3174", code: "EE3174", title: "Embedded Systems Lab", section: "L01",
      instructor: "Instructor C", email: "instructor-c@example.edu", accent: "purple",
      description: "Practice embedded C, microcontroller peripherals, and hardware debugging.",
      announcements: [
        { id: "a1", date: "2026-10-24", title: "Lab equipment", body: "Remember your development board and a USB cable for the next lab." }
      ],
      assignments: [
        { id: "lab7", title: "Lab 7: Timer Capture", due: "2026-10-29", points: 50, score: null, status: "In progress", feedback: null },
        { id: "lab6", title: "Lab 6: Timers", due: "2026-10-19", points: 50, score: 44, status: "Graded", feedback: "Timer configuration works. Add comments around the interrupt logic." }
      ]
    },
    {
      id: "cs4321", code: "CS4321", title: "Algorithms", section: "R02",
      instructor: "Instructor D", email: "instructor-d@example.edu", accent: "blue",
      description: "Study graph algorithms, greedy methods, and dynamic programming.",
      announcements: [
        { id: "a1", date: "2026-10-22", title: "Upcoming homework", body: "Show your reasoning and analyze the running time for each algorithm." }
      ],
      assignments: [
        { id: "hw4", title: "Homework 4", due: "2026-11-02", points: 100, score: null, status: "Not submitted", feedback: null },
        { id: "hw3", title: "Homework 3", due: "2026-10-14", points: 100, score: 86, status: "Graded", feedback: "Correct recurrence. Explain the base case more clearly." }
      ]
    },
    {
      id: "cs4611", code: "CS4611", title: "Computer Graphics", section: "R01",
      instructor: "Instructor E", email: "instructor-e@example.edu", accent: "rose",
      description: "Develop interactive graphics using rendering, geometry, and OpenGL.",
      announcements: [
        { id: "a1", date: "2026-10-24", title: "Rendering project", body: "Show a running scene and be prepared to discuss the drawing pipeline." }
      ],
      assignments: [
        { id: "city", title: "InfiniCity", due: "2026-11-03", points: 100, score: null, status: "In progress", feedback: null },
        { id: "merry", title: "Merry-go-round", due: "2026-10-20", points: 100, score: 92, status: "Graded", feedback: "Scene is functional. The camera movement could be smoother." }
      ]
    }
  ]
};

if(typeof window !== "undefined"){
  window.JAME_DATA = JAME_DATA;
}
if(typeof module !== "undefined"){
  module.exports = JAME_DATA;
}
