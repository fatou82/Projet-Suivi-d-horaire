import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JournalAuditComponent } from './journal-audit.component';

describe('JournalAuditComponent', () => {
  let component: JournalAuditComponent;
  let fixture: ComponentFixture<JournalAuditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JournalAuditComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JournalAuditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
