import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GererPostesComponent } from './gerer-postes.component';

describe('GererPostesComponent', () => {
  let component: GererPostesComponent;
  let fixture: ComponentFixture<GererPostesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GererPostesComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GererPostesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
