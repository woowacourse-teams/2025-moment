describe("Authentication", () => {
  beforeEach(() => {
    cy.clearLocalStorage();
  });

  describe("Login Flow", () => {
    it("should redirect to login when accessing protected route", () => {
      cy.visit("/dashboard");
      cy.url().should("include", "/login");
    });

    it("should login as ADMIN and access dashboard", () => {
      cy.loginAsAdmin();
      cy.url().should("include", "/dashboard");
      cy.contains("ADMIN").should("be.visible");
    });

    it("should login as VIEWER and access dashboard", () => {
      cy.loginAsViewer();
      cy.url().should("include", "/dashboard");
      cy.contains("VIEWER").should("be.visible");
    });

    it("should logout successfully", () => {
      cy.loginAsAdmin();
      cy.contains("Logout").click();
      cy.url().should("include", "/login");
    });
  });

  describe("Permission Denial Flow (VIEWER)", () => {
    beforeEach(() => {
      cy.loginAsViewer();
    });

    it("VIEWER should not see admin-only action buttons", () => {
      // This test will be expanded when admin-only features are implemented
      cy.visit("/dashboard");
      cy.contains("VIEWER").should("be.visible");
    });
  });
});
