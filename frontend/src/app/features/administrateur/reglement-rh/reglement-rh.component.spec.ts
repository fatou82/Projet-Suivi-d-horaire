import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReglementRhComponent } from './reglement-rh.component';

describe('ReglementRhComponent', () => {
  let component: ReglementRhComponent;
  let fixture: ComponentFixture<ReglementRhComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReglementRhComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReglementRhComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
