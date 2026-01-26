// Cypress E2E support file

// Custom commands
Cypress.Commands.add('login', (email: string, password: string) => {
  cy.visit('/login');
  cy.get('input[type="email"]').type(email);
  cy.get('input[type="password"]').type(password);
  cy.get('button[type="submit"]').click();
});

Cypress.Commands.add('loginAsAdmin', () => {
  cy.login('admin@moment.com', 'admin');
});

Cypress.Commands.add('loginAsViewer', () => {
  cy.login('viewer@moment.com', 'viewer');
});

// Type declarations
declare global {
  namespace Cypress {
    interface Chainable {
      login(email: string, password: string): Chainable<void>;
      loginAsAdmin(): Chainable<void>;
      loginAsViewer(): Chainable<void>;
    }
  }
}

export {};
